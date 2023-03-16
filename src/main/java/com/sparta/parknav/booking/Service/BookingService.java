package com.sparta.parknav.booking.Service;

import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.global.exception.CustomException;
import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.MsgType;
import com.sparta.parknav.global.response.ResponseUtils;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;

    public ApiResponseDto<BookingInfoResponseDto> getInfoBeforeBooking(Long id, BookingInfoRequestDto requestDto) {

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );

        String available;
        // 현재 운영여부 확인
        if (checkOperation(LocalDateTime.now().getDayOfWeek(), parkOperInfo)) {
            // 현재 주차 가능 대수 = 주차 가능 대수 - 출차시간이 없는 현황 수(주차중인 경우)
            available = (parkOperInfo.getCmprtCo() - parkMgtInfoRepository.countByParkInfoIdAndExitTimeIsNull(id)) + "대";
        } else {
            // 운영중이 아니라면 메시지 출력
            available = MsgType.NOT_OPEN_NOW.getMsg();
        }

        String booking;
        // 선택한 날짜 운영여부 확인
        if (checkOperation(requestDto.getStartDate().getDayOfWeek(), parkOperInfo)
                && checkOperation(requestDto.getEndDate().getDayOfWeek(), parkOperInfo)) {
            // 선택시간 예약건수 = 기존 예약 중에서 사용자가 선택한 시간 사이에 예약 시작 시간이 있거나 예약 끝나는 시간이 있는 경우
            booking = parkBookingInfoRepository.getBookingCnt(id, requestDto.getStartDate(), requestDto.getEndDate()) + "대";
        } else {
            // 선택시간이 운영중이 아니라면 메시지 출력
            booking = MsgType.NOT_OPEN_SELECT_DATE.getMsg();
        }

        // 주차 시간 구하기
        int bookingTime = getBookingTime(requestDto);

        // 주차 요금 = ((주차 시간 - 기본 시간) / 추가시간) * 추가요금 + 기본요금
        int charge = ((bookingTime - parkOperInfo.getChargeBsTime()) / parkOperInfo.getChargeAditUnitTime()) * parkOperInfo.getChargeAditUnitChrg() + parkOperInfo.getChargeBsChrg();

        BookingInfoResponseDto responseDto = BookingInfoResponseDto.of(available, booking, charge);

        return ResponseUtils.ok(responseDto, MsgType.SEARCH_SUCCESSFULLY);
    }


    private boolean checkOperation(DayOfWeek dayOfWeek, ParkOperInfo parkOperInfo) {

        if (dayOfWeek == DayOfWeek.SATURDAY
                && parkOperInfo.getSatOpen().equals("00:00") && parkOperInfo.getSatClose().equals("00:00")) {
            return false;
        } else if (dayOfWeek == DayOfWeek.SUNDAY
                && parkOperInfo.getSunOpen().equals("00:00") && parkOperInfo.getSunClose().equals("00:00")) {
            return false;
        } else if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                && parkOperInfo.getWeekdayOpen().equals("00:00") && parkOperInfo.getWeekdayClose().equals("00:00")) {
            return false;
        } else {
            return true;
        }

    }

    private static int getBookingTime(BookingInfoRequestDto requestDto) {

        int parkingDay = requestDto.getEndDate().getDayOfYear() - requestDto.getStartDate().getDayOfYear();
        int parkingTime = (requestDto.getEndDate().getHour() * 60 + requestDto.getEndDate().getMinute())
                - (requestDto.getStartDate().getHour() * 60 + requestDto.getStartDate().getMinute());

        if (parkingTime < 0) {
            parkingDay -= 1;
            parkingTime += 1440;
        }

        return  parkingDay * 1440 + parkingTime;
    }

}
