package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.handler.TransactionHandler;
import com.sparta.parknav.booking.entity.ParkBookingByHour;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingByHourRepository;
import com.sparta.parknav.booking.repository.ParkBookingByHourRepositoryCustom;
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
import java.util.*;
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
    private final ParkBookingByHourRepositoryCustom parkBookingByHourRepositoryCustom;
    private final BookingService bookingService;
    private final TransactionHandler transactionHandler;

    public CarInResponseDto enter(CarNumRequestDto requestDto, Admin user) {
        if (requestDto.getParkId() == null) {
            throw new CustomException(ErrorType.CONTENT_IS_NULL);
        }
        return redisLockRepository.runOnLock(
                requestDto.getParkId(),
                ()->transactionHandler.runOnWriteTransaction(() -> enterLogic(requestDto, user)));
    }

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
        Optional<ParkMgtInfo> alreadyEnterInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumAndExitTimeNullOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum());
        if (alreadyEnterInfo.isPresent()) {
            throw new CustomException(ErrorType.ALREADY_ENTER_CAR);
        }

        // SCENARIO ENTER 4
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkInfo.getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // SCENARIO ENTER 5
        LocalDateTime now = LocalDateTime.now();
        Optional<ParkBookingInfo> parkBookingNow = parkBookingInfoRepository
                .findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndEndTimeGreaterThan(parkInfo.getId(), requestDto.getCarNum(), now, now);

        // SCENARIO ENTER 5-1
        Optional<ParkBookingInfo> parkBookingPlusHour = parkBookingInfoRepository
                .findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndEndTimeGreaterThan(parkInfo.getId(), requestDto.getCarNum(), now.plusHours(1), now.plusHours(1));

        ParkBookingInfo bookingInfo;
        // SCENARIO ENTER 5
        if (parkBookingNow.isPresent()) {
            bookingInfo = parkBookingNow.get();
        // SCENARIO ENTER 5-1
        } else if (parkBookingPlusHour.isPresent()) {
            bookingInfo = getUpdatedBookingInfo(parkInfo, now, parkOperInfo, parkBookingPlusHour.get());
        } else {
            checkOverlappedTime(requestDto, parkInfo, now);
            bookingInfo = bookingService.bookingParkNow(parkInfo, now, now.plusHours(requestDto.getParkingTime()), requestDto.getCarNum());
        }

        // SCENARIO ENTER 6
        ParkBookingByHour parkBookingByHour = parkBookingByHourRepository.findByParkInfoIdAndDateAndTime(parkInfo.getId(), now.toLocalDate(), now.getHour());
        int availableCnt = parkBookingByHour != null ? parkBookingByHour.getAvailable() : parkOperInfo.getCmprtCo();
        int parkingCarsCount = parkMgtInfoRepository.countByParkInfoIdAndExitTimeIsNull(parkInfo.getId());
        if (availableCnt < 0 || parkingCarsCount >= parkOperInfo.getCmprtCo()) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        }

        // SCENARIO ENTER 7
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

        // SCENARIO EXIT 2
        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumAndExitTimeNullOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR_IN_PARK)
        );

        // SCENARIO EXIT 3
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkMgtInfo.getParkInfo().getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // SCENARIO EXIT 4
        LocalDateTime now = LocalDateTime.now();
        ParkBookingInfo bookingInfo = parkMgtInfo.getParkBookingInfo();
        long minutes = Duration.between(bookingInfo.getStartTime(), now).toMinutes();

        if (bookingInfo.getUser() != null) {
            minutes = Duration.between(bookingInfo.getStartTime(), bookingInfo.getEndTime()).toMinutes();
        }

        int charge = ParkingFeeCalculator.calculateParkingFee(minutes, parkOperInfo);

        // SCENARIO EXIT 5
        List<ParkBookingByHour> hourList = parkBookingByHourRepositoryCustom.findByParkInfoIdAndFromStartDateToEndDate(parkOperInfo.getParkInfo().getId(), now, bookingInfo.getEndTime());
        hourList.forEach(hour -> hour.updateCnt(1));

        // SCENARIO EXIT 6
        bookingInfo.endTimeUpdate(now);

        parkMgtInfo.update(charge, now);

        return CarOutResponseDto.of(charge, now);
    }

    @Transactional
    public ParkMgtListResponseDto mgtPage(Admin admin, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Optional<ParkInfo> parkInfo = parkInfoRepository.findById(admin.getParkInfo().getId());
        String parkName = parkInfo.get().getName();
        Long parkId = parkInfo.get().getId();
        Page<ParkMgtInfo> parkMgtInfos = parkMgtInfoRepository.findAllByParkInfoIdOrderByEnterTimeDesc(admin.getParkInfo().getId(), pageable);
        List<ParkMgtResponseDto> parkMgtResponseDtos = parkMgtInfos.stream()
                .map(p -> ParkMgtResponseDto.of(p.getCarNum(), p.getEnterTime(), p.getExitTime(), p.getCharge()))
                .collect(Collectors.toList());

        Page page1 = new PageImpl(parkMgtResponseDtos, pageable, parkMgtInfos.getTotalElements());
        return ParkMgtListResponseDto.of(page1, parkName,parkId);
    }


    private ParkBookingInfo getUpdatedBookingInfo(ParkInfo parkInfo, LocalDateTime now, ParkOperInfo parkOperInfo, ParkBookingInfo parkBookingPlusHour) {
        // 즉시 예약 로직이 아닌 기존 예약에 시간을 추가하는것이기 때문에 주차공간이 있는지 여부를 검증하고 available을 추가해줘야함
        ParkBookingByHour parkBookingByHour = parkBookingByHourRepository.findByParkInfoIdAndDateAndTime(parkInfo.getId(), now.toLocalDate(), now.getHour());
        if (parkBookingByHour == null) {
            parkBookingByHourRepository.save(ParkBookingByHour.of(now.toLocalDate(), now.getHour(), parkOperInfo.getCmprtCo() - 1, parkOperInfo.getParkInfo()));
        } else if (parkBookingByHour.getAvailable() <= 0) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        } else {
            parkBookingByHour.updateCnt(-1);
        }

        parkBookingPlusHour.startTimeUpdate(now);

        return parkBookingPlusHour;
    }

    private void checkOverlappedTime(CarNumRequestDto requestDto, ParkInfo parkInfo, LocalDateTime now) {

        if (requestDto.getParkingTime() <= 0) {
            throw new CustomException(ErrorType.PARK_TIME_NOT_EXIST);
        }

        ParkBookingInfo parkBookingInfo = parkBookingInfoRepository.findTopByParkInfoIdAndCarNumAndStartTimeGreaterThan(parkInfo.getId(), requestDto.getCarNum(), now);
        List<LocalDateTime> notAllowedTimeList = parkBookingInfo != null ?
                findOverlappedTime(parkBookingInfo.getStartTime(), parkBookingInfo.getEndTime(), now, now.plusHours(requestDto.getParkingTime()))
                : Collections.emptyList();

        if (!notAllowedTimeList.isEmpty()) {
            throw new CustomException(ErrorType.printLocalDateTimeList(notAllowedTimeList));
        }
    }

    public List<LocalDateTime> findOverlappedTime(LocalDateTime start1, LocalDateTime end1,
                                                  LocalDateTime start2, LocalDateTime end2) {

        List<LocalDateTime> betweenTime = new ArrayList<>();
        if (start1.isAfter(end1) || start2.isAfter(end2)) {
            throw new IllegalArgumentException("Invalid input: start cannot be after end");
        }

        if (start1.isEqual(end1) || start2.isEqual(end2)) {
            return Collections.emptyList();
        }

        LocalDateTime start3 = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime end3 = end1.isBefore(end2) ? end1 : end2;

        if (start3.isAfter(end3)) {
            return Collections.emptyList();
        }
        betweenTime.add(start3);
        betweenTime.add(end3);
        return betweenTime;
    }

}
