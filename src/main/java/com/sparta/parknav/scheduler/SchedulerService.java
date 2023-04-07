package com.sparta.parknav.scheduler;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ParkBookingByHourRepository parkBookingByHourRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    public void scheduleRunTestPre(List<ParkMgtInfo> parkMgtInfos) {

        for (ParkMgtInfo p : parkMgtInfos) {
            ParkBookingInfo parkBookingInfo = p.getParkBookingInfo();
            int endTime = parkBookingInfo.getEndTime().getHour();
            ParkBookingByHour parkBookingByHour = parkBookingByHourRepository
                    .findByParkInfoIdAndDateAndTime(p.getParkInfo().getId(), parkBookingInfo.getEndTime().plusHours(1).toLocalDate(), endTime + 1);

            System.out.println("id = " + p.getId());
            System.out.println("carNum = " + p.getCarNum());
            System.out.println("현재 예약 종료 시간 = " + p.getParkBookingInfo().getEndTime());
            if (parkBookingByHour != null) {
                System.out.println("현재 예약 종료 시간 + 1시간의 parkBookingByHour.available  = " + parkBookingByHour.getAvailable());
            } else {
                System.out.println("한 시간 뒤의 parkBookingByHour.available은 존재하지 않습니다.");
            }
        }
    }

    public void scheduleRunTestPost(List<ParkMgtInfo> parkMgtInfos) {

        for (ParkMgtInfo p : parkMgtInfos) {
            ParkBookingInfo parkBookingInfo = p.getParkBookingInfo();
            int endTime = parkBookingInfo.getEndTime().getHour();
            ParkBookingByHour parkBookingByHour = parkBookingByHourRepository
                    .findByParkInfoIdAndDateAndTime(p.getParkInfo().getId(), parkBookingInfo.getEndTime().toLocalDate(), endTime);

            System.out.println("id = " + p.getId());
            System.out.println("carNum = " + p.getCarNum());
            System.out.println("parkBookingByHour.getId() = " + parkBookingByHour.getId());
            System.out.println("이후 예약 종료 시간 = " + p.getParkBookingInfo().getEndTime());
            if (parkBookingByHour != null) {
                System.out.println("이후 시간의 parkBookingByHour.available  = " + parkBookingByHour.getAvailable());
            } else {
                System.out.println("한 시간 뒤의 parkBookingByHour.available은 존재하지 않습니다.");
            }
        }
    }

    @Transactional
    @Scheduled(cron = "* * */1 * * *")
    public void scheduleRun() {

        List<ParkMgtInfo> parkMgtInfos = parkMgtInfoRepository.findAllByExitTimeIsNullAndParkBookingInfoEndTimeBefore(LocalDateTime.now());
        scheduleRunTestPre(parkMgtInfos);

        for (ParkMgtInfo p : parkMgtInfos) {
            ParkBookingInfo parkBookingInfo = p.getParkBookingInfo();
            ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(p.getParkInfo().getId()).orElseThrow(
                    () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
            );

            parkBookingInfo.endTimeUpdate(parkBookingInfo.getEndTime().getHour() + 1);
            int endTime = parkBookingInfo.getEndTime().getHour();
            ParkBookingByHour parkBookingByHour = parkBookingByHourRepository
                    .findByParkInfoIdAndDateAndTime(p.getParkInfo().getId(), parkBookingInfo.getEndTime().toLocalDate(), endTime);

            if (parkBookingByHour != null) {
                parkBookingByHour.updateCnt(-1);
            } else {
                parkBookingByHour = ParkBookingByHour
                        .of(parkBookingInfo.getEndTime().toLocalDate(), endTime, parkOperInfo.getCmprtCo() - 1, p.getParkInfo());
                parkBookingByHourRepository.save(parkBookingByHour);
            }
        }
        scheduleRunTestPost(parkMgtInfos);
    }
}
