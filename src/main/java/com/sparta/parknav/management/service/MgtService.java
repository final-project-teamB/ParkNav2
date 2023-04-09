package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.dto.NomalBookingCarSpaceInfo;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtListResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.redis.RedisLockRepository;
import com.sparta.parknav.user.entity.Admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MgtService {

    private final RedisLockRepository redisLockRepository;
    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;

    @Transactional
    public CarInResponseDto enter(CarNumRequestDto requestDto, Admin user) {

        // SCENARIO ENTER 1
        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }

        // SCENARIO ENTER 2
        ParkInfo parkInfo = parkInfoRepository.findById(requestDto.getParkId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );

        // SCENARIO ENTER 3
        Optional<ParkMgtInfo> park = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum());
        if (park.isPresent() && park.get().getExitTime() == null) {
            throw new CustomException(ErrorType.ALREADY_ENTER_CAR);
        }

        while (!redisLockRepository.lock(requestDto.getParkId())) {
            // SpinLock 방식이 Redis 에게 주는 부하를 줄여주기 위한 sleep
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CustomException(ErrorType.FAILED_TO_ACQUIRE_LOCK);
            }
        }
        try {
            // SCENARIO ENTER 4
            int cmprtCoNum = parkInfo.getParkOperInfo().getCmprtCo();
            NomalBookingCarSpaceInfo nomalBookingCarSpaceInfo = nomalBookingCarSpaceInfo(cmprtCoNum);

            // SCENARIO ENTER 5
            List<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findAllByParkInfoId(requestDto.getParkId());
            NomalBookingCarSpaceInfo useNomalBookingCarSpaceInfo = useNomalBookingCarSpaceInfo(parkMgtInfo);

            // SCENARIO ENTER 6
            LocalDateTime now = LocalDateTime.now();
            Optional<ParkBookingInfo> enterCarBookingInfo = parkBookingInfoRepository.findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndEndTimeGreaterThan(requestDto.getParkId(), requestDto.getCarNum(), now, now);

            // SCENARIO ENTER 7
            ParkBookingInfo parkBookingNow = null;
            if (enterCarBookingInfo.isPresent()) {
                // 예약구역이 꽉 찼을 경우
                if (nomalBookingCarSpaceInfo.getBookingCarSpace() <= useNomalBookingCarSpaceInfo.getBookingCarSpace()) {
                    throw new CustomException(ErrorType.NOT_PARKING_SPACE);
                }
                parkBookingNow = enterCarBookingInfo.get();
            } else {
                // 일반구역이 꽉 찼을 경우
                if (nomalBookingCarSpaceInfo.getNomalCarSpace() <= useNomalBookingCarSpaceInfo.getNomalCarSpace()) {
                    throw new CustomException(ErrorType.NOT_PARKING_SPACE);
                }
            }

            ParkMgtInfo mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, null, 0, parkBookingNow);
            parkMgtInfoRepository.save(mgtSave);

            return CarInResponseDto.of(requestDto.getCarNum(), now);

        } finally {
            // Lock 해제
            redisLockRepository.unlock(requestDto.getParkId());
        }
    }

    @Transactional
    public CarOutResponseDto exit(CarNumRequestDto requestDto, Admin user) {

        // SCENARIO EXIT 1
        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }

        // SCENARIO EXIT 2
        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );
        // SCENARIO EXIT 3
        if (parkMgtInfo.getExitTime() != null) {
            throw new CustomException(ErrorType.ALREADY_TAKEN_OUT_CAR);
        }

        // SCENARIO EXIT 4
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(parkMgtInfo.getEnterTime(), now);
        long minutes = duration.toMinutes();
        ParkOperInfo parkOperInfo = parkMgtInfo.getParkInfo().getParkOperInfo();
        int charge = ParkingFeeCalculator.calculateParkingFee(minutes, parkOperInfo);

        // SCENARIO EXIT 5
        parkMgtInfo.update(charge, now);

        return CarOutResponseDto.of(charge, now);
    }

    @Transactional
    public ParkMgtListResponseDto mgtPage(Admin admin, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Optional<ParkInfo> parkInfo = parkInfoRepository.findById(admin.getParkInfo().getId());
        String parkName = parkInfo.get().getName();
        Page<ParkMgtInfo> parkMgtInfos = parkMgtInfoRepository.findAllByParkInfoIdOrderByEnterTimeDesc(admin.getParkInfo().getId(), pageable);
        List<ParkMgtResponseDto> parkMgtResponseDtos = parkMgtInfos.stream()
                .map(p -> ParkMgtResponseDto.of(p.getCarNum(), p.getEnterTime(), p.getExitTime(), p.getCharge()))
                .collect(Collectors.toList());

        Page page1 = new PageImpl(parkMgtResponseDtos, pageable, parkMgtInfos.getTotalElements());
        return ParkMgtListResponseDto.of(page1, parkName);
    }


    private static NomalBookingCarSpaceInfo nomalBookingCarSpaceInfo(int cmprtCoNum) {
        int nomalCarSpace = cmprtCoNum % 2 == 1 ? (cmprtCoNum / 2) + 1 : cmprtCoNum / 2;
        int bookingCarSpace = cmprtCoNum / 2;
        return NomalBookingCarSpaceInfo.of(nomalCarSpace, bookingCarSpace);
    }

    private static NomalBookingCarSpaceInfo useNomalBookingCarSpaceInfo(List<ParkMgtInfo> parkMgtInfos) {
        int nomalCarSpace = 0;
        int bookingCarSpace = 0;
        for (ParkMgtInfo parkMgtInfo : parkMgtInfos) {
            if (parkMgtInfo.getExitTime() == null && parkMgtInfo.getParkBookingInfo() == null) {
                nomalCarSpace++;
            } else if (parkMgtInfo.getExitTime() == null) {
                bookingCarSpace++;
            }
        }
        return NomalBookingCarSpaceInfo.of(nomalCarSpace, bookingCarSpace);
    }
}
