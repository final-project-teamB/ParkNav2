package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
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
            List<ParkBookingInfo> parkBookingInfo = parkBookingInfoRepository.findAllByParkInfoId(requestDto.getParkId());
            List<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findAllByParkInfoId(requestDto.getParkId());
            LocalDateTime now = LocalDateTime.now();
            int bookingNowCnt = getBookingNowCnt(requestDto.getCarNum(), parkBookingInfo, now, parkMgtInfo);

            // SCENARIO ENTER 5
            ParkBookingInfo parkBookingNow = getParkBookingInfo(requestDto, parkBookingInfo, now);
            if (parkBookingNow != null && parkMgtInfoRepository.existsByParkBookingInfoIdAndExitTimeIsNotNull(parkBookingNow.getId())) {
                parkBookingNow = null;
            }

            // SCENARIO ENTER 6
            int cmprtCoNum = parkInfo.getParkOperInfo().getCmprtCo();
            int mgtNum = getMgtNum(parkMgtInfo);
            if (bookingNowCnt + mgtNum >= cmprtCoNum) {
                throw new CustomException(ErrorType.NOT_PARKING_SPACE);
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
        LocalDateTime now = LocalDateTime.now();
        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );

        // SCENARIO EXIT 3
        if (parkMgtInfo.getExitTime() != null) {
            throw new CustomException(ErrorType.ALREADY_TAKEN_OUT_CAR);
        }

        // SCENARIO EXIT 4
        ParkOperInfo parkOperInfo = parkMgtInfo.getParkInfo().getParkOperInfo();
        Duration duration = Duration.between(parkMgtInfo.getEnterTime(), now);
        long minutes = duration.toMinutes();
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

    private static ParkBookingInfo getParkBookingInfo(CarNumRequestDto requestDto, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now) {

        ParkBookingInfo parkBookingNow = null;
        for (ParkBookingInfo p : parkBookingInfo) {
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                if (Objects.equals(p.getCarNum(), requestDto.getCarNum())) {
                    parkBookingNow = p;
                    break;
                }
            }
        }
        return parkBookingNow;
    }

    private static int getBookingNowCnt(String carNum, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now, List<ParkMgtInfo> parkMgtInfo) {

        int bookingNowCnt = 0;
        for (ParkBookingInfo p : parkBookingInfo) {
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                if (Objects.equals(p.getCarNum(), carNum)) {
                    continue;
                }
                bookingNowCnt++;
            }
        }
        // 예약된 차량이 주차장에 이미 입차되어 있는지 확인
        for (ParkBookingInfo p : parkBookingInfo) {
            // 현재 시간이 예약 시작 시간-1보다 크고, 예약 종료 시간보다 작을때
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                for (ParkMgtInfo m : parkMgtInfo) {
                    // 예약차가 입차해있고, 입차된 차량의 예약번호와 같을 때
                    if (Objects.equals(m.getCarNum(), p.getCarNum()) && Objects.equals(m.getParkBookingInfo().getId(), p.getId())) {
                        bookingNowCnt--;
                    }
                }
            }
        }
        return bookingNowCnt;
    }

    private static int getMgtNum(List<ParkMgtInfo> parkMgtInfo) {

        int mgtNum = 0;
        for (ParkMgtInfo p : parkMgtInfo) {
            if (p.getExitTime() == null) {
                mgtNum++;
            }
        }
        return mgtNum;
    }
}
