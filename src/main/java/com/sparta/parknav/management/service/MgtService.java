package com.sparta.parknav.management.service;

import com.sparta.parknav.global.exception.CustomException;
import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.MsgType;
import com.sparta.parknav.global.response.ResponseUtils;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkMgtInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.ticket.entity.ParkBookingInfo;
import com.sparta.parknav.ticket.repository.ParkBookingInfoRepository;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MgtService {
    
    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;

    @Transactional
    public ApiResponseDto<CarInResponseDto> enter(CarNumRequestDto requestDto) {

        // 이 주차장에 예약된 모든 list를 통한 현재 예약된 차량수 구하기
        List<ParkBookingInfo> parkBookingInfo = parkBookingInfoRepository.findAllByParkInfoId(requestDto.getParkId());
        LocalDateTime now = LocalDateTime.now();
        int nowInt = Integer.parseInt(now.format(DateTimeFormatter.ofPattern("MMddHHmmss")));
        // 입차하려는 현재 예약이 되어있는 차량수(예약자가 입차할 경우 -1)
        int bookingNowCnt = getBookingNowCnt(requestDto.getCarNum(), parkBookingInfo, nowInt);
 
        ParkInfo parkInfo = parkInfoRepository.findById(requestDto.getParkId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );
        // 주차 구획수
        int cmprtCoNum = parkInfo.getParkOperInfo().getCmprtCo();
        List<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findAllByParkInfoId(requestDto.getParkId());
        // 이 주차장에 현재 입차되어있는 차량 수
        int mgtNum = getMgtNum(nowInt, parkMgtInfo);
        if (bookingNowCnt + mgtNum == cmprtCoNum) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        }

        ParkMgtInfo mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, null, 0,null);
        parkMgtInfoRepository.save(mgtSave);

        return ResponseUtils.ok(CarInResponseDto.of(requestDto.getCarNum(),now), MsgType.ENTER_SUCCESSFULLY);
    }

    public ApiResponseDto<CarOutResponseDto> exit(CarNumRequestDto requestDto) {


        return null;
    }

    public ApiResponseDto<ParkMgtResponseDto> mgtPage(User user) {

        return null;
    }

    private static int getBookingNowCnt(String carNum, List<ParkBookingInfo> parkBookingInfo, int nowInt) {

        int bookingNowCnt = 0;
        for (ParkBookingInfo p : parkBookingInfo) {
            int bookingStart = Integer.parseInt(p.getStartTime().minusHours(1).format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            int bookingEnd = Integer.parseInt(p.getEndTime().format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            if (bookingStart >= nowInt && bookingEnd <= nowInt) {
                if (Objects.equals(p.getCarNum(), carNum)) {
                    continue;
                }
                bookingNowCnt++;
            }
        }
        return bookingNowCnt;
    }

    private static int getMgtNum(int nowInt, List<ParkMgtInfo> parkMgtInfo) {

        int mgtNum = 0;
        for (ParkMgtInfo p : parkMgtInfo) {
            int parkStart = Integer.parseInt(p.getEnterTime().minusHours(1).format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            if (p.getExitTime() == null) {
                mgtNum++;
                continue;
            }
            int parkEnd = Integer.parseInt(p.getExitTime().format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            if (parkStart >= nowInt && parkEnd <= nowInt) {
                mgtNum++;
            }
        }
        return mgtNum;
    }
}
