package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.dto.ParkSpaceInfo;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtListResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.entity.ZoneType;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
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

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    final double GENERAL_RATE = 0.4;
    final double BOOKING_RATE = 0.4;

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
        // LOCKING ENTER
        Optional<ParkMgtInfo> alreadyEnterInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum());
        if (alreadyEnterInfo.isPresent() && alreadyEnterInfo.get().getExitTime() == null) {
            throw new CustomException(ErrorType.ALREADY_ENTER_CAR);
        }

        // SCENARIO ENTER 4
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkInfo.getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // SCENARIO ENTER 5
        // 주차장의 구역 수
        ParkSpaceInfo parkSpaceInfo = getParkSpaceInfo(parkOperInfo);
        // 구역별 현재 주차중인 차량 수
        ParkSpaceInfo useSpaceInfo = getUseSpaceInfo(parkInfo);

        // SCENARIO ENTER 6
        LocalDateTime now = LocalDateTime.now();
        Optional<ParkBookingInfo> enterCarBookingInfo = parkBookingInfoRepository.findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndEndTimeGreaterThan(requestDto.getParkId(), requestDto.getCarNum(), now, now);

        // SCENARIO ENTER 7
        ParkMgtInfo mgtSave;
        // 예약차량이며 해당 예약내역으로 주차장을 사용하지 않은 경우
        if (enterCarBookingInfo.isPresent() && !parkMgtInfoRepository.existsByParkBookingInfoIdAndExitTimeIsNotNull(enterCarBookingInfo.get().getId())) {
            mgtSave = getParkMgtBookingCar(requestDto, parkInfo, parkOperInfo, parkSpaceInfo, useSpaceInfo, now, enterCarBookingInfo);
            // 일반차량인 경우
        } else {
            mgtSave = getParkMgtGeneralCar(requestDto, parkInfo, parkSpaceInfo, useSpaceInfo, now);
        }

        parkMgtInfoRepository.save(mgtSave);

        return CarInResponseDto.of(requestDto.getCarNum(), now, mgtSave.getZone());
    }

    @Transactional
    public CarOutResponseDto exit(CarNumRequestDto requestDto, Admin user) {

        // SCENARIO EXIT 1
        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }

        // SCENARIO EXIT 2
        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_ENTER_CAR)
        );

        // SCENARIO EXIT 3
        if (parkMgtInfo.getExitTime() != null) {
            throw new CustomException(ErrorType.ALREADY_TAKEN_OUT_CAR);
        }

        // SCENARIO EXIT 4
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkMgtInfo.getParkInfo().getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // SCENARIO EXIT 5
        ParkBookingInfo bookingInfo = parkMgtInfo.getParkBookingInfo();

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(parkMgtInfo.getEnterTime(), now).toMinutes();

        // SCENARIO EXIT 6
        if (bookingInfo != null) {
            // 예약종료시간 이전에 출차해도 요금은 예약종료시간까지로 계산하며, 예약종료시간 이후에 출차하면 예약시작시간부터 출차한 시간까지로 요금을 계산한다.
            LocalDateTime chargeEndTime = now.isBefore(bookingInfo.getEndTime()) ? bookingInfo.getEndTime() : now;
            minutes = Duration.between(bookingInfo.getStartTime(), chargeEndTime).toMinutes();
            // 예약 종료 시간을 현재시간으로 변경
            bookingInfo.endTimeUpdate(now);
        }

        // SCENARIO EXIT 7
        if (parkMgtInfo.getZone() == ZoneType.GENERAL) {
            Optional<ParkMgtInfo> generalCarInCommon = parkMgtInfoRepository
                    .findTopByParkInfoIdAndZoneAndExitTimeNullOrderByEnterTimeAsc(requestDto.getParkId(), ZoneType.COMMON);

            generalCarInCommon.ifPresent(mgtInfo -> mgtInfo.updateZone(ZoneType.GENERAL));
        }

        // SCENARIO EXIT 8
        int charge = ParkingFeeCalculator.calculateParkingFee(minutes, parkOperInfo);
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
        return ParkMgtListResponseDto.of(page1, parkName, parkId);
    }


    public ParkSpaceInfo getUseSpaceInfo(ParkInfo parkInfo) {
        // LOCKING ENTER
        int generalUseCnt = parkMgtInfoRepository.countByParkInfoIdAndZoneAndExitTimeIsNull(parkInfo.getId(), ZoneType.GENERAL);
        int bookingUseCnt = parkMgtInfoRepository.countByParkInfoIdAndZoneAndExitTimeIsNull(parkInfo.getId(), ZoneType.BOOKING);
        int commonUseCnt = parkMgtInfoRepository.countByParkInfoIdAndZoneAndExitTimeIsNull(parkInfo.getId(), ZoneType.COMMON);
        return ParkSpaceInfo.of(generalUseCnt, bookingUseCnt, commonUseCnt);
    }

    public ParkSpaceInfo getParkSpaceInfo(ParkOperInfo parkOperInfo) {
        int cmprtCoNum = parkOperInfo.getCmprtCo();
        int generalZoneCnt = (int) (cmprtCoNum * GENERAL_RATE);
        int bookingZoneCnt = (int) (cmprtCoNum * BOOKING_RATE);
        int commonZoneCnt = cmprtCoNum - (generalZoneCnt + bookingZoneCnt);
        return ParkSpaceInfo.of(generalZoneCnt, bookingZoneCnt, commonZoneCnt);
    }

    private static ParkMgtInfo getParkMgtGeneralCar(CarNumRequestDto requestDto, ParkInfo parkInfo, ParkSpaceInfo parkSpaceInfo, ParkSpaceInfo useSpaceInfo, LocalDateTime now) {
        ParkMgtInfo mgtSave;
        // 일반구역 자리가 있는 경우
        if (parkSpaceInfo.getGeneralCarSpace() > useSpaceInfo.getGeneralCarSpace()) {
            mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, ZoneType.GENERAL);
            // 일반구역 자리 없고 공통 구역 자리 있는 경우
        } else if (parkSpaceInfo.getCommonCarSpace() > useSpaceInfo.getCommonCarSpace()) {
            mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, ZoneType.COMMON);
            // 일반구역, 공통구역 모두 자리 없는 경우
        } else {
            throw new CustomException(ErrorType.NOT_PARKING_COMMON_SPACE);
        }

        return mgtSave;
    }

    private static ParkMgtInfo getParkMgtBookingCar(CarNumRequestDto requestDto, ParkInfo parkInfo, ParkOperInfo parkOperInfo, ParkSpaceInfo parkSpaceInfo, ParkSpaceInfo useSpaceInfo, LocalDateTime now, Optional<ParkBookingInfo> enterCarBookingInfo) {
        // 예약구역 자리 없는 경우
        if (parkSpaceInfo.getBookingCarSpace() <= useSpaceInfo.getBookingCarSpace()) {
            throw new CustomException(ErrorType.NOT_PARKING_BOOKING_SPACE);
        }

        // 예약차량인 경우 입차시 예약시간에 대한 요금을 미리 보여준다.
        long bookingTime = Duration.between(enterCarBookingInfo.get().getStartTime(), enterCarBookingInfo.get().getEndTime()).toMinutes();
        int charge = ParkingFeeCalculator.calculateParkingFee(bookingTime, parkOperInfo);

        return ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, charge, enterCarBookingInfo.get(), ZoneType.BOOKING);
    }
}
