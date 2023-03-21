package com.sparta.parknav.booking.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.dto.BookingResponseDto;
import com.sparta.parknav.booking.dto.MyBookingResponseDto;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.entity.StatusType;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final CarRepository carRepository;

    public ApiResponseDto<BookingInfoResponseDto> getInfoBeforeBooking(Long id, BookingInfoRequestDto requestDto) {

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(id).orElseThrow(
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

    public ApiResponseDto<BookingResponseDto> bookingPark(Long parkId, BookingInfoRequestDto requestDto, User user) {
        // SCENARIO BOOKING 1
        if (requestDto.getStartDate().equals(requestDto.getEndDate())||requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new CustomException(ErrorType.NOT_END_TO_START);
        }
        // SCENARIO BOOKING 2
        ParkInfo parkInfo = parkInfoRepository.findById(parkId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );
        // SCENARIO BOOKING 3
        Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        ); // 2~5 , 1~3
        // SCENARIO BOOKING 4
        List<ParkBookingInfo> parkBookingInfo = parkBookingInfoRepository.findAllByParkInfoAndUser(parkInfo, user);
        for (ParkBookingInfo p : parkBookingInfo) {
            if (p != null) {
                if (requestDto.getStartDate().isBefore(p.getEndTime())&&requestDto.getEndDate().isAfter(p.getStartTime())) {
                    throw new CustomException(ErrorType.ALREADY_RESERVED);
                }
            }
        }

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

    public ApiResponseDto<Page<MyBookingResponseDto>> getMyBooking(User user, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ParkBookingInfo> bookingInfoList = parkBookingInfoRepository.findAllByUserIdOrderByStartTimeDesc(user.getId(),pageable);
        List<MyBookingResponseDto> responseDtoList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (ParkBookingInfo p : bookingInfoList) {

            // 요금 계산을 위해 예약 시작, 종료 시간 차이를 구한다.
            long minutes = Duration.between(p.getStartTime(), p.getEndTime()).toMinutes();

            // status 초기값 '사용완료'
            StatusType status = StatusType.USED;
            // 예약 내역으로 주차장에 주차했는지 확인
            Optional<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findByParkBookingInfoId(p.getId());
            // 예약내역으로 주차하지 않은 경우
            if (parkMgtInfo.isEmpty()) {
                // 현재 시간이 예약종료시간 전이라면 '미사용', 현재 시간이 예약종료시간을 지났다면 '기간만료'
                status = now.isBefore(p.getEndTime()) ? StatusType.UNUSED : StatusType.EXPIRED;
            }

            // 예약내역으로 주차 출차까지 한 경우, 요금계산을 위해 실제 사용시간을 구한다.
            if (parkMgtInfo.isPresent() && parkMgtInfo.get().getExitTime() != null) {
                minutes = Duration.between(parkMgtInfo.get().getEnterTime(), parkMgtInfo.get().getExitTime()).toMinutes();
            }

            int charge = ParkingFeeCalculator.calculateParkingFee(minutes, p.getParkInfo().getParkOperInfo());

            MyBookingResponseDto responseDto = MyBookingResponseDto.of(p, charge, status);
            responseDtoList.add(responseDto);
        }

        return ResponseUtils.ok(new PageImpl<>(responseDtoList,pageable,bookingInfoList.getTotalElements()), MsgType.SEARCH_SUCCESSFULLY);
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

        return parkingDay * 1440 + parkingTime;
    }

}
