package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParkBookingInfoRepository extends JpaRepository<ParkBookingInfo,Long> {

    List<ParkBookingInfo> findAllByParkInfoId(Long parkId);

    Page<ParkBookingInfo> findAllByUserIdOrderByStartTimeDesc(Long id, Pageable pageable);

    @Query(value = "SELECT p FROM ParkBookingInfo p WHERE p.parkInfo.id = :parkId " +
            "AND ((p.startTime >= :startTime and p.startTime < :endTime) " +
            "OR (p.endTime > :startTime and p.endTime <= :endTime)" +
            "OR (p.startTime <= :startTime and p.endTime >= :endTime))")
    List<ParkBookingInfo> getSelectedTimeBookingList(@Param("parkId") Long parkId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<ParkBookingInfo> findAllByParkInfoIdAndUserId(Long parkInfoId, Long userId);

    List<ParkBookingInfo> findAllByParkInfoIdAndUserIdAndCarNum(Long parkInfoId, Long userId, String carNum);
}
