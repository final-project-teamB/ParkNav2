package com.sparta.parknav.management.service;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.dto.ParkSpaceInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.entity.ZoneType;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class SchedueldMgtService {

    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final MgtService mgtService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void moveCarsToDesignatedArea() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime DateTimeHour = LocalDateTime.of(now.toLocalDate(), LocalTime.of(now.getHour(), 0, 0));
        LocalDateTime startTime = DateTimeHour.minusMinutes(59).minusSeconds(59);

        // 예약시작시간이 현재시간 00시 00분인 예약정보를 모두 가져옴
        List<ParkBookingInfo> bookingInfoStartTimeCarList = parkBookingInfoRepository.findAllByStartTimeEquals(DateTimeHour);
        for (ParkBookingInfo bookingInfoStartTimeCar : bookingInfoStartTimeCarList) {
            // 가져온 예약정보 중 이미 입차한 차량 중 일반구획에 있는 차량 리스트를 가져옴
            Optional<ParkMgtInfo> parkMgtInfoCheck = parkMgtInfoRepository.findByParkInfoIdAndCarNumAndExitTimeNullAndZone(bookingInfoStartTimeCar.getParkInfo().getId(), bookingInfoStartTimeCar.getCarNum(), ZoneType.GENERAL);
            if (parkMgtInfoCheck.isPresent()) {
                ParkMgtInfo parkMgtInfo = parkMgtInfoCheck.get();
                Optional<ParkOperInfo> parkOperInfoCheck = parkOperInfoRepository.findByParkInfoId(bookingInfoStartTimeCar.getParkInfo().getId());
                // 주차장 구획 별 여유공간을 가져옴
                ParkSpaceInfo useSpaceInfo = mgtService.getUseSpaceInfo(bookingInfoStartTimeCar.getParkInfo());
                ParkSpaceInfo spaceInfo = ParkSpaceInfo.of(0, 0, 0);
                if (parkOperInfoCheck.isPresent()) {
                    spaceInfo = mgtService.getParkSpaceInfo(parkOperInfoCheck.get());
                }
                // 여유 공간이 있을때만 이동
                if (useSpaceInfo.getBookingCarSpace() < spaceInfo.getBookingCarSpace()) {
                    // 차량 구획을 예약구획으로 이동
                    parkMgtInfo.updateZone(ZoneType.BOOKING);
                }
            }
        }

        // 예약 종료 시간이 현재시간 -59분 59초부터 00시 00분까지 예약정보를 모두 가져옴
        List<ParkBookingInfo> bookingInfoEndTimeCarList = parkBookingInfoRepository.findAllByEndTimeBetween(startTime, DateTimeHour);
        for (ParkBookingInfo bookingInfoEndTimeCar : bookingInfoEndTimeCarList) {
            // 예약시간이 초과 된 예약정보 중 아직 출차하지 않고 예약구획에 있는 차량 관리정보를 가져옴
            Optional<ParkMgtInfo> parkMgtInfoCheck = parkMgtInfoRepository.findByParkBookingInfoIdAndExitTimeNullAndZone(bookingInfoEndTimeCar.getId(), ZoneType.BOOKING);
            if (parkMgtInfoCheck.isPresent()) {
                ParkMgtInfo parkMgtInfo = parkMgtInfoCheck.get();
                Optional<ParkOperInfo> parkOperInfoCheck = parkOperInfoRepository.findByParkInfoId(bookingInfoEndTimeCar.getParkInfo().getId());
                // 주차장 구획 별 여유공간을 가져옴
                ParkSpaceInfo useSpaceInfo = mgtService.getUseSpaceInfo(bookingInfoEndTimeCar.getParkInfo());
                ParkSpaceInfo spaceInfo = ParkSpaceInfo.of(0, 0, 0);
                if (parkOperInfoCheck.isPresent()) {
                    spaceInfo = mgtService.getParkSpaceInfo(parkOperInfoCheck.get());
                }
                // 여유 공간이 있을때만 이동
                if (useSpaceInfo.getCommonCarSpace() < spaceInfo.getCommonCarSpace()) {
                    // 차량을 공통 구획으로 이동
                    parkMgtInfo.updateZone(ZoneType.COMMON);
                }
            }
        }
    }
}
