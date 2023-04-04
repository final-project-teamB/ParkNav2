package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.dto.NomalBookingCarSpaceInfo;
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
import com.sparta.parknav.redis.RedisLockRepository;
import com.sparta.parknav.user.entity.Admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MgtService {

    private final RedisLockRepository redisLockRepository;
    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final RedissonClient redissonClient;

//    public CarInResponseDto enter(CarNumRequestDto requestDto, Admin user) {
//        if (requestDto.getParkId() == null) {
//            throw new CustomException(ErrorType.CONTENT_IS_NULL);
//        }
//        int MAX_RETRIES = 5;
//        int RETRY_INTERVAL_MS = 50;
//        RLock lock = redissonClient.getLock("EnterLock" + requestDto.getParkId());
//        boolean lockAcquired = false;
//        int retries = 0;
//        while (!lockAcquired && retries < MAX_RETRIES) {
//            try {
//                if (lock.tryLock(10, 10, TimeUnit.SECONDS)) {
//                    lockAcquired = true;
//                } else {
//                    retries++;
//                    log.info("락 획득 실패, 재시도 중 ({}/{})", retries, MAX_RETRIES);
//                    Thread.sleep(RETRY_INTERVAL_MS);
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new CustomException(ErrorType.INTERRUPTED_WHILE_WAITING_FOR_LOCK);
//            }
//        }
//
//        if (!lockAcquired) {
//            log.info("락 획득 실패");
//            throw new CustomException(ErrorType.FAILED_TO_ACQUIRE_LOCK);
//        }
//
//        try {
//            log.info("락 획득 성공");
//            return enterLogic(requestDto, user);
//        } finally {
//            if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//                log.info("언락 실행");
//            }
//        }
//    }
    public CarInResponseDto enter(CarNumRequestDto requestDto, Admin user) {
        if (requestDto.getParkId() == null) {
            throw new CustomException(ErrorType.CONTENT_IS_NULL);
        }
        RLock lock = redissonClient.getLock("EnterLock" + requestDto.getParkId());
        try {
            //선행 락 점유 스레드가 존재하면 waitTime동안 락 점유를 기다리며 leaseTime 시간 이후로는 자동으로 락이 해제되기 때문에 다른 스레드도 일정 시간이 지난 후 락을 점유할 수 있습니다.
            if (!lock.tryLock(30, 10, TimeUnit.SECONDS)) {
                log.info("락 획득 실패");
                throw new CustomException(ErrorType.FAILED_TO_ACQUIRE_LOCK);
            }
            log.info("락 획득 성공");
            return enterLogic(requestDto, user);
        } catch (InterruptedException e) {
            log.info("락 획득 대기 중 인터럽트 발생");
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorType.INTERRUPTED_WHILE_WAITING_FOR_LOCK);
        } finally {
            log.info("finally문 실행");
            if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("언락 실행");
            }
        }
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

        // 이 주차장에 예약된 모든 list를 통한 현재 예약된 차량수 구하기
        // SCENARIO ENTER 4
        LocalDateTime now = LocalDateTime.now();
        List<ParkBookingInfo> parkBookingInfo = parkBookingInfoRepository.findAllByParkInfoIdAndEndTimeAfter(requestDto.getParkId(), now);
        List<ParkMgtInfo> parkMgtInfo = parkMgtInfoRepository.findAllByParkInfoId(requestDto.getParkId());

        // 입차하려는 현재 예약이 되어있는 차량수(예약자가 입차할 경우 -1)
        int bookingNowCnt = getBookingNowCnt(requestDto.getCarNum(), parkBookingInfo, now, parkMgtInfo);
        // 예약된 차량 찾기
        ParkBookingInfo parkBookingNow = getParkBookingInfo(requestDto, parkBookingInfo, now);
        // 이미 예약내역으로 입차, 출차를 마친 경우는 예약시간 내 입차해도 일반차량으로 분류된다.
        // SCENARIO ENTER 6
        if (parkBookingNow != null && parkMgtInfoRepository.existsByParkBookingInfoIdAndExitTimeIsNotNull(parkBookingNow.getId())) {
            parkBookingNow = null;
        }
        // 주차 구획수
        int cmprtCoNum = parkInfo.getParkOperInfo().getCmprtCo();
        // 이 주차장에 현재 입차되어있는 차량 수
        int mgtNum = getMgtNum(parkMgtInfo);
        if (bookingNowCnt + mgtNum >= cmprtCoNum) {
            throw new CustomException(ErrorType.NOT_PARKING_SPACE);
        }

        ParkMgtInfo mgtSave = ParkMgtInfo.of(parkInfo, requestDto.getCarNum(), now, null, 0, parkBookingNow);
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
        ParkOperInfo parkOperInfo = parkMgtInfo.getParkInfo().getParkOperInfo();

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

    private static ParkBookingInfo getParkBookingInfo(CarNumRequestDto requestDto, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now) {

        ParkBookingInfo parkBookingNow = null;
        for (ParkBookingInfo p : parkBookingInfo) {
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                if (Objects.equals(p.getCarNum(), requestDto.getCarNum())) {
                    parkBookingNow = p;
                    break;
                }
            }
        }
        return parkBookingNow;
    }

    private static int getBookingNowCnt(String carNum, List<ParkBookingInfo> parkBookingInfo, LocalDateTime now, List<ParkMgtInfo> parkMgtInfo) {
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
        // 예약된 차량이 주차장에 이미 입차되어 있는지 확인
        for (ParkBookingInfo p : parkBookingInfo) {
            // 현재 시간이 예약 시작 시간-1보다 크고, 예약 종료 시간보다 작을때
            if ((p.getStartTime().minusHours(1).isEqual(now) || p.getStartTime().minusHours(1).isBefore(now)) && p.getEndTime().isAfter(now)) {
                for (ParkMgtInfo m : parkMgtInfo) {
                    // 예약차가 입차해있고, 입차된 차량의 예약번호와 같을 때
                    if (Objects.equals(m.getCarNum(), p.getCarNum()) && Objects.equals(m.getParkBookingInfo().getId(), p.getId())) {
                        bookingNowCnt--;
                    }
                }
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

    private static NomalBookingCarSpaceInfo nomalBookingCarSpaceInfo(int cmprtCoNum) {
        int nomalCarSpace = cmprtCoNum % 2 == 1 ? (cmprtCoNum / 2) + 1 : cmprtCoNum / 2;
        int bookingCarSpace = cmprtCoNum / 2;
        return NomalBookingCarSpaceInfo.of(nomalCarSpace, bookingCarSpace);
    }

    private static NomalBookingCarSpaceInfo useNomalBookingCarSpaceInfo(List<ParkMgtInfo> parkMgtInfos) {
        int nomalCarSpace = 0;
        int bookingCarSpace = 0;
        for (ParkMgtInfo parkMgtInfo : parkMgtInfos) {
            if (parkMgtInfo.getExitTime() == null && parkMgtInfo.getParkBookingInfo() == null) {
                nomalCarSpace++;
            } else if (parkMgtInfo.getExitTime() == null) {
                bookingCarSpace++;
            }
        }
        return NomalBookingCarSpaceInfo.of(nomalCarSpace, bookingCarSpace);
    }
}
