package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingByHour;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingByHourRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.booking.service.BookingService;
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
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
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
    private final ParkBookingByHourRepository parkBookingByHourRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    private final BookingService bookingService;

    public CarInResponseDto enter(CarNumRequestDto requestDto, Admin user) {
//        TransactionHandler transactionHandler = new TransactionHandler(transactionTemplate);
//        while (true) {
//            if (!redisLockRepository.lock(requestDto.getParkId())) {
//                // SpinLock 방식이 Redis 에게 주는 부하를 줄여주기 위한 sleep
//                try {
//                    log.info("락 획득 실패");
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    throw new CustomException(ErrorType.FAILED_TO_ACQUIRE_LOCK);
//                }
//            } else {
//                log.info("락 획득 성공, lock number : {}", requestDto.getParkId());
//                break;
//            }
//        }
//        try {
            return enterLogic(requestDto, user);
//        } finally {
//            // Lock 해제
//            redisLockRepository.unlock(requestDto.getParkId());
//        }
    }


    @Transactional
    public CarInResponseDto enterLogic(CarNumRequestDto requestDto, Admin user) {

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

        LocalDateTime now = LocalDateTime.now();
        ParkBookingInfo bookingInfo;
        // 예약된 차량 찾기
        Optional<ParkBookingInfo> parkBookingNow = parkBookingInfoRepository.findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndEndTimeGreaterThan(parkInfo.getId(), requestDto.getCarNum(), now, now);
        // 예약된 차량이 아니라면 즉시 예약을 시도한다.
        if (parkBookingNow.isEmpty()) {
            bookingInfo = bookingService.bookingParkNow(parkInfo, LocalDateTime.now(), LocalDateTime.now().plusHours(requestDto.getParkingTime()), requestDto.getCarNum());
        } else {
            bookingInfo = parkBookingNow.get();
        }

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkInfo.getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // 현재 시간대 주차 가능대수를 확인한다.
        ParkBookingByHour parkBookingByHour = parkBookingByHourRepository.findByParkInfoIdAndDateAndTime(parkInfo.getId(), now.toLocalDate(), now.getHour());
        int availableCnt = parkBookingByHour!=null ? parkBookingByHour.getAvailable() : parkOperInfo.getCmprtCo();
        if (availableCnt < 0 || parkMgtInfoRepository.countByParkInfoIdAndExitTimeIsNull(parkInfo.getId()) >= parkOperInfo.getCmprtCo()) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        }

        // 요금 계산
        int charge = ParkingFeeCalculator.calculateParkingFee(Duration.between(bookingInfo.getStartTime(), bookingInfo.getEndTime()).toMinutes(), parkOperInfo);

        ParkMgtInfo mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, null, charge, bookingInfo);
        parkMgtInfoRepository.save(mgtSave);

        return CarInResponseDto.of(requestDto.getCarNum(), now);
    }

    @Transactional
    public CarOutResponseDto exit(CarNumRequestDto requestDto, Admin user) {
        // SCENARIO EXIT 1
        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }

        LocalDateTime now = LocalDateTime.now();
        // SCENARIO EXIT 2
        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );
        // SCENARIO EXIT 3
        if (parkMgtInfo.getExitTime() != null) {
            throw new CustomException(ErrorType.ALREADY_TAKEN_OUT_CAR);
        }

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkMgtInfo.getParkInfo().getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        Duration duration = Duration.between(parkMgtInfo.getEnterTime(), now);
        long minutes = duration.toMinutes();
        // SCENARIO EXIT 4
        int charge = ParkingFeeCalculator.calculateParkingFee(minutes, parkOperInfo);

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
}
