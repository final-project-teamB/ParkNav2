package com.sparta.parknav.management.repository;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.entity.ZoneType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo,Long> {

    Page<ParkMgtInfo> findAllByParkInfoIdOrderByEnterTimeDesc(Long parkId, Pageable pageable);

    int countByParkInfoIdAndExitTimeIsNull(Long id);

    // LOCKING
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkMgtInfo> findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(Long parkId, String carNum);

    Optional<ParkMgtInfo> findByParkBookingInfoId(Long id);

    Boolean existsByParkBookingInfoIdAndExitTimeIsNotNull(Long bookingInfoId);

    //LOCKING
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    int countByParkInfoIdAndZoneAndExitTimeIsNull(Long parkInfoId, ZoneType zone);

    Optional<ParkMgtInfo> findByParkBookingInfoIdAndExitTimeNullAndZone(Long parkBookingInfoId, ZoneType zone);

    Optional<ParkMgtInfo> findByParkInfoIdAndCarNumAndExitTimeNullAndZone(Long parkInfoId, String carNum, ZoneType zone);

    Optional<ParkMgtInfo> findTopByParkInfoIdAndZoneAndExitTimeNullOrderByEnterTimeAsc(Long parkInfoId, ZoneType zone);

    int countByParkBookingInfoInAndExitTimeNotNull(List<ParkBookingInfo> bookingInfoList);

}
