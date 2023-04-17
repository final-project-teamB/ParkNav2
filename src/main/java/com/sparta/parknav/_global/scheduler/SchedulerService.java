package com.sparta.parknav._global.scheduler;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.booking.entity.ParkBookingByHour;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.ParkBookingByHourRepository;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class SchedulerService {

    private final ParkBookingByHourRepository parkBookingByHourRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    @Transactional
    @PreAuthorize("permitAll()")
    @Scheduled(cron = "0 * * * * *")
    public void scheduleRun() {
        List<ParkMgtInfo> parkMgtInfos = parkMgtInfoRepository.findAllByExitTimeIsNullAndParkBookingInfoExitTimeBefore(LocalDateTime.now());
        for (ParkMgtInfo p : parkMgtInfos) {
            ParkBookingInfo parkBookingInfo = p.getParkBookingInfo();
            if (parkBookingInfo == null) {
                throw new CustomException(ErrorType.NOT_FOUND_BOOKING);
            }
            ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(p.getParkInfo().getId()).orElseThrow(
                    () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
            );
            parkBookingInfo.exitTimePlus(1);
            LocalDateTime endDateTime = parkBookingInfo.getExitTime();
            int endTime = endDateTime.getHour();
            ParkBookingByHour parkBookingByHour = parkBookingByHourRepository
                    .findByParkInfoIdAndDateAndTime(p.getParkInfo().getId(), endDateTime.toLocalDate(), endTime);

            if (parkBookingByHour != null) {
                parkBookingByHour.updateCnt(-1);
            } else {
                parkBookingByHour = ParkBookingByHour
                        .of(endDateTime.toLocalDate(), endTime, parkOperInfo.getCmprtCo() - 1, p.getParkInfo());
                parkBookingByHourRepository.save(parkBookingByHour);
            }
        }
    }
}
