package com.sparta.parknav.booking.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.dto.BookingResponseDto;
import com.sparta.parknav.booking.dto.MyBookingResponseDto;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.entity.StatusType;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.dto.ParkSpaceInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.MgtService;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final CarRepository carRepository;
    private final RedissonClient redissonClient;

    private final MgtService mgtService;


    public BookingInfoResponseDto getInfoBeforeBooking(Long id, BookingInfoRequestDto requestDto) {

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );

        // 주차 시간 구하기
        long bookingTime = Duration.between(requestDto.getStartDate(), requestDto.getEndDate()).toMinutes();
        // 주차 요금 구하기
        int charge = ParkingFeeCalculator.calculateParkingFee(bookingTime, parkOperInfo);

        // 선택한 날짜 운영여부 확인
        boolean isOperation = OperationChecking.checkOperation(requestDto.getStartDate(), parkOperInfo) && OperationChecking.checkOperation(requestDto.getEndDate(), parkOperInfo);
        // 선택 날짜에 운영하지 않는다면 notAllowedTimeList는 빈 배열을 바로 리턴한다.
        if (!isOperation) {
            return BookingInfoResponseDto.of(new ArrayList<>(), charge, false);
        }

        // 시간별 예약가능 여부를 확인하여 불가한 경우의 시간을 List에 담는다.
        List<LocalDateTime> notAllowedTimeList = getNotAllowedTimeList(id, requestDto, parkOperInfo);

        return BookingInfoResponseDto.of(notAllowedTimeList, charge, true);
    }

    @Transactional
    public BookingResponseDto bookingPark(Long parkId, BookingInfoRequestDto requestDto, User user) {

        // SCENARIO BOOKING 1
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new CustomException(ErrorType.NOT_END_TO_START);
        }

        // SCENARIO BOOKING 2
        ParkInfo parkInfo = parkInfoRepository.findById(parkId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK)
        );

        // SCENARIO BOOKING 3
        Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_CAR)
        );

        // SCENARIO BOOKING 4
        ParkBookingInfo alreadyBookingInfo = parkBookingInfoRepository.getAlreadyBookingInfo(parkInfo.getId(), car.getCarNum(), requestDto.getStartDate(), requestDto.getEndDate());
        if (alreadyBookingInfo != null) {
            throw new CustomException(ErrorType.ALREADY_RESERVED);
        }
        // LOCKING RESERVATION
        // SCENARIO BOOKING 5
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // SCENARIO BOOKING 6
        List<LocalDateTime> notAllowedTimeList = getNotAllowedTimeList(parkId, requestDto, parkOperInfo);
        if (notAllowedTimeList.size() > 0) {
            throw new CustomException(ErrorType.NOT_ALLOWED_BOOKING_TIME);
        }

        ParkBookingInfo bookingInfo = ParkBookingInfo.of(requestDto, user, parkInfo, car.getCarNum());

        return BookingResponseDto.of(parkBookingInfoRepository.save(bookingInfo).getId());
    }

    @Transactional
    public void cancelBooking(Long id, User user) {

        ParkBookingInfo bookingInfo = parkBookingInfoRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_BOOKING)
        );

        if (!bookingInfo.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorType.NOT_BOOKING_USER);
        }

        parkBookingInfoRepository.deleteById(id);
    }

    public Page<MyBookingResponseDto> getMyBooking(User user, int page, int size) {

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

            // 예약시간보다 오래 주차하고 출차한 경우, 요금계산을 위해 실제 사용시간을 구한다. 예약시간 이전에 출차했다면 예약한 시간만큼 요금을 받는다.
            if (parkMgtInfo.isPresent() && parkMgtInfo.get().getExitTime() != null && parkMgtInfo.get().getExitTime().isAfter(p.getEndTime())) {
                minutes = Duration.between(parkMgtInfo.get().getEnterTime(), parkMgtInfo.get().getExitTime()).toMinutes();
            }

            ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(p.getParkInfo().getId()).orElseThrow(
                    () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
            );

            int charge = ParkingFeeCalculator.calculateParkingFee(minutes, parkOperInfo);

            MyBookingResponseDto responseDto = MyBookingResponseDto.of(p, charge, status);
            responseDtoList.add(responseDto);
        }

        return new PageImpl<>(responseDtoList,pageable,bookingInfoList.getTotalElements());
    }


    private List<LocalDateTime> getNotAllowedTimeList(Long id, BookingInfoRequestDto requestDto, ParkOperInfo parkOperInfo) {

        List<LocalDateTime> notAllowedTimeList = new ArrayList<>();

        ParkSpaceInfo spaceInfo = mgtService.getParkSpaceInfo(parkOperInfo);

        long hours = Duration.between(requestDto.getStartDate(), requestDto.getEndDate()).toHours();
        LocalDateTime start = requestDto.getStartDate();
        // 선택한 시간만큼 반복
        for (int i = 0; i < hours; i++) {
            // 선택 시작 시간부터 한시간 단위로 예약 건수를 구한다.
            LocalDateTime time = start.plusHours(i);
            // LOCKING RESERVATION
            // 선택시간 사이 예약정보
            List<ParkBookingInfo> bookingList = parkBookingInfoRepository.getSelectedTimeBookingList(id, time, time.plusHours(1));
            // 예약정보 중 이미 출차한 차량 수는 제외한다.
            int bookingCnt = bookingList.size() - parkMgtInfoRepository.countByParkBookingInfoInAndExitTimeNotNull(bookingList);
            // 예약 건수가 예약 구역수보다 같거나 크면, 예약불가 시간 리스트에 추가한다.
            if (bookingCnt >= spaceInfo.getBookingCarSpace()) {
                notAllowedTimeList.add(time);
            }
        }

        return notAllowedTimeList;
    }

}
