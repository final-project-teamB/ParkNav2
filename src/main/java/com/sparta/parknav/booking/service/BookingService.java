package com.sparta.parknav.booking.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.dto.BookingResponseDto;
import com.sparta.parknav.booking.dto.MyBookingResponseDto;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.entity.ParkBookingByHour;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.entity.StatusType;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.booking.repository.ParkBookingByHourRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final CarRepository carRepository;
    private final ParkBookingByHourRepository parkBookingByHourRepository;

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

    public BookingResponseDto bookingPark(Long parkId, BookingInfoRequestDto requestDto, User user) {
        // SCENARIO BOOKING 1
        if (!requestDto.getStartDate().isBefore(requestDto.getEndDate())) {
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

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );
        // 시간별 예약가능 여부를 확인하여 불가한 경우의 시간을 List에 담는다.
        List<LocalDateTime> notAllowedTimeList = getNotAllowedTimeList(parkId, requestDto, parkOperInfo);

        if (notAllowedTimeList.size() > 0) {
            throw new CustomException(ErrorType.NOT_ALLOWED_BOOKING_TIME);
        }

        ParkBookingInfo bookingInfo = ParkBookingInfo.of(requestDto, user, parkInfo, car.getCarNum());

        parkBookingByHourSave(parkId, parkOperInfo, requestDto);

        return BookingResponseDto.of(parkBookingInfoRepository.save(bookingInfo).getId());
    }

    @Transactional
    public ParkBookingInfo bookingParkNow(ParkInfo parkInfo,  LocalDateTime startTime, LocalDateTime endTime, String carNum) {

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkInfo.getId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        // 선택시간 운영여부 확인
        boolean isOperation = OperationChecking.checkOperation(startTime, parkOperInfo) && OperationChecking.checkOperation(endTime, parkOperInfo);
        if (!isOperation) {
            throw new CustomException(ErrorType.NOT_OPEN_SELECTED_DATE);
        }

        BookingInfoRequestDto bookingInfoRequestDto = BookingInfoRequestDto.of(startTime, endTime);
        // 시간별 예약가능 여부 확인
        List<LocalDateTime> notAllowedTimeList = getNotAllowedTimeList(parkInfo.getId(), bookingInfoRequestDto, parkOperInfo);
        if (notAllowedTimeList.size() > 0) {
            throw new CustomException(ErrorType.printLocalDateTimeList(notAllowedTimeList));
        }

        ParkBookingInfo bookingInfo = ParkBookingInfo.of(bookingInfoRequestDto, parkInfo, carNum);

        parkBookingByHourSave(parkInfo.getId(), parkOperInfo, bookingInfoRequestDto);

        return parkBookingInfoRepository.save(bookingInfo);

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

        LocalDateTime startDateTime = requestDto.getStartDate();
        LocalDate startDate = requestDto.getStartDate().toLocalDate();
        LocalDateTime endDateTime = requestDto.getEndDate();
        LocalDate endDate = requestDto.getEndDate().toLocalDate();

        // 예약 리스트에서 설정한 날짜 사이 주차공간이 0인 시간이 있다면 가져온다
        List<ParkBookingByHour> bookingInfos = parkBookingByHourRepository.findByParkInfoIdAndDateBetweenAndAvailableEquals(id, startDate, endDate, 0);

        for (ParkBookingByHour bookingInfo : bookingInfos) {
            LocalDateTime bookingDateTime = LocalDateTime.of(bookingInfo.getDate(), LocalTime.of(bookingInfo.getTime(), 0, 0));
            // 리스트 중 시작, 종료 시간이 같거나 사이일 경우 주차불가능 리스트로 반환한다
            if (bookingDateTime.isEqual(startDateTime) || bookingDateTime.isEqual(endDateTime) ||
                    bookingDateTime.isAfter(startDateTime) && bookingDateTime.isBefore(endDateTime)) {
                notAllowedTimeList.add(bookingDateTime);
            }
        }
        return notAllowedTimeList;
    }

    public void parkBookingByHourSave(Long id, ParkOperInfo parkOperInfo, BookingInfoRequestDto requestDto) {

        List<ParkBookingByHour> parkBookingByHourList = new ArrayList<>();
        // 시간차이를 구한다
        long hours = Duration.between(requestDto.getStartDate(), requestDto.getEndDate()).toHours();
        if (requestDto.getStartDate().getMinute() > 0 || requestDto.getStartDate().getSecond() > 0) {
            hours += 1;
        }
        // 시작시간을 구한다
        LocalDateTime start = requestDto.getStartDate();
        // 총 시간만큼 For문을 순회한다
        for (int i = 0; i < hours; i++) {
            LocalDateTime time = start.plusHours(i);
            // 기존에 저장 된 ParkBookingByHour가 있으면 available -1 없을경우 새로 저장한다
            Optional<ParkBookingByHour> parkBookingByHourExist = parkBookingByHourRepository.findByParkInfoIdAndDateAndTime(id, time.toLocalDate(), time.getHour());
            if (parkBookingByHourExist.isPresent()){
                parkBookingByHourExist.get().updateCnt(-1);
            } else {
                parkBookingByHourList.add(ParkBookingByHour.of(time.toLocalDate(), time.getHour(), parkOperInfo.getCmprtCo() - 1, parkOperInfo.getParkInfo()));
            }
        }
        if (parkBookingByHourList.size() > 0){
            parkBookingByHourRepository.saveAll(parkBookingByHourList);
        }
    }

}
