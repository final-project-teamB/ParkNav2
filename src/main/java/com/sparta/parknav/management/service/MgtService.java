package com.sparta.parknav.management.service;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkMgtInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.user.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MgtService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;

    @Transactional
    public ApiResponseDto<CarInResponseDto> enter(CarNumRequestDto requestDto, Admin user) {

        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }
        if (parkMgtInfoRepository.findByParkInfoIdAndCarNum(requestDto.getParkId(),requestDto.getCarNum()).isPresent()) {
            throw new CustomException(ErrorType.ALREADY_ENTER_CAR);
        }

        // 이 주차장에 예약된 모든 list를 통한 현재 예약된 차량수 구하기
        List<ParkBookingInfo> parkBookingInfo = parkBookingInfoRepository.findAllByParkInfoId(requestDto.getParkId());
        LocalDateTime now = LocalDateTime.now();
        // 입차하려는 현재 예약이 되어있는 차량수(예약자가 입차할 경우 -1)
        int bookingNowCnt = getBookingNowCnt(requestDto.getCarNum(), parkBookingInfo, now);
        // 예약된 차량 찾기
        ParkBookingInfo parkBookingNow = getParkBookingInfo(requestDto, parkBookingInfo, now);

        ParkInfo parkInfo = parkInfoRepository.findById(requestDto.getParkId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );
        // 주차 구획수
        int cmprtCoNum = parkInfo.getParkOperInfo().getCmprtCo();
        List<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findAllByParkInfoId(requestDto.getParkId());
        // 이 주차장에 현재 입차되어있는 차량 수
        int mgtNum = getMgtNum(parkMgtInfo);
        if (bookingNowCnt + mgtNum >= cmprtCoNum) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        }

        ParkMgtInfo mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, null, 0, parkBookingNow);
        parkMgtInfoRepository.save(mgtSave);

        return ResponseUtils.ok(CarInResponseDto.of(requestDto.getCarNum(), now), MsgType.ENTER_SUCCESSFULLY);
    }

    @Transactional
    public ApiResponseDto<CarOutResponseDto> exit(CarNumRequestDto requestDto, Admin user) {

        if (!Objects.equals(requestDto.getParkId(), user.getParkInfo().getId())) {
            throw new CustomException(ErrorType.NOT_MGT_USER);
        }

        LocalDateTime now = LocalDateTime.now();

        ParkMgtInfo parkMgtInfo = parkMgtInfoRepository.findByParkInfoIdAndCarNum(requestDto.getParkId(), requestDto.getCarNum()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );

        if (parkMgtInfo.getExitTime() != null) {
            throw new CustomException(ErrorType.ALREADY_TAKEN_OUT_CAR);
        }
        ParkOperInfo parkOperInfo = parkMgtInfo.getParkInfo().getParkOperInfo();

        Duration duration = Duration.between(parkMgtInfo.getEnterTime(), now);
        long minutes = duration.toMinutes();

        int charge = ParkingFeeCalculator.calculateParkingFee(minutes,parkOperInfo);

        parkMgtInfo.update(charge, now);
        return ResponseUtils.ok(CarOutResponseDto.of(charge, now), MsgType.EXIT_SUCCESSFULLY);
    }

    @Transactional
    public ApiResponseDto<List<ParkMgtResponseDto>> mgtPage(Admin admin) {
        List<ParkMgtInfo> parkMgtInfos = parkMgtInfoRepository.findAllByParkInfoId(admin.getParkInfo().getId());
        List<ParkMgtResponseDto> parkMgtResponseDtos = new ArrayList<>();
        for (ParkMgtInfo p : parkMgtInfos) {
            parkMgtResponseDtos.add(ParkMgtResponseDto.of(p.getCarNum(), p.getEnterTime(), p.getExitTime(), p.getCharge()));
        }
        return ResponseUtils.ok(parkMgtResponseDtos, MsgType.SEARCH_SUCCESSFULLY);
    }

    private static ParkBookingInfo getParkBookingInfo(CarNumRequestDto requestDto, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now) {
        ParkBookingInfo parkBookingNow = null;
        for (ParkBookingInfo p : parkBookingInfo) {
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                if (Objects.equals(p.getCarNum(), requestDto.getCarNum())) {
                    parkBookingNow = p;
                }
            }
        }
        return parkBookingNow;
    }

    private static int getBookingNowCnt(String carNum, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now) {
        // 3시 입차
        // 2~5시 예약
        int bookingNowCnt = 0;
        for (ParkBookingInfo p : parkBookingInfo) {
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                if (Objects.equals(p.getCarNum(), carNum)) {
                    continue;
                }
                bookingNowCnt++;
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
