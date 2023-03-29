package com.sparta.parknav.management.repository;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo,Long> {
    
//    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    List<ParkMgtInfo> findAllByParkInfoId(Long parkId);

    Page<ParkMgtInfo> findAllByParkInfoIdOrderByEnterTimeDesc(Long parkId, Pageable pageable);

    int countByParkInfoIdAndExitTimeIsNull(Long id);

    Optional<ParkMgtInfo> findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(Long parkId, String carNum);

    Optional<ParkMgtInfo> findByParkBookingInfoId(Long id);

    int countByParkBookingInfoIn(List<ParkBookingInfo> bookingInfoList);

    Boolean existsByParkBookingInfoIdAndExitTimeIsNotNull(Long bookingInfoId);
}
