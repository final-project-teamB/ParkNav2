package com.sparta.parknav.booking.service;

import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.dto.BookingResponseDto;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final CarRepository carRepository;

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
        // 주차 요금 구하기
        int charge = ParkingFeeCalculator.calculateParkingFee(bookingTime, parkOperInfo);

        BookingInfoResponseDto responseDto = BookingInfoResponseDto.of(available, booking, charge);

        return ResponseUtils.ok(responseDto, MsgType.SEARCH_SUCCESSFULLY);
    }

    public ApiResponseDto<BookingResponseDto> bookingPark(Long id, BookingInfoRequestDto requestDto, User user) {

        // 선택한 시간이 현재 시간 이전인 경우 예외처리
        if (LocalDateTime.now().compareTo(requestDto.getStartDate()) > 0) {
            throw new CustomException(ErrorType.NOT_AVAILABLE_TIME);
        }

        ParkInfo parkInfo = parkInfoRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );

        Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );

        ParkBookingInfo bookingInfo = ParkBookingInfo.of(requestDto, user, parkInfo, car.getCarNum());

        return ResponseUtils.ok(BookingResponseDto.of(parkBookingInfoRepository.save(bookingInfo).getId()), MsgType.BOOKING_SUCCESSFULLY);
    }

    @Transactional
    public ApiResponseDto<Void> cancelBooking(Long id, User user) {

        ParkBookingInfo bookingInfo = parkBookingInfoRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_BOOKING)
        );

        if (!bookingInfo.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorType.NOT_BOOKING_USER);
        }

        parkBookingInfoRepository.deleteById(id);

        return ResponseUtils.ok(MsgType.CANCEL_SUCCESSFULLY);
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
