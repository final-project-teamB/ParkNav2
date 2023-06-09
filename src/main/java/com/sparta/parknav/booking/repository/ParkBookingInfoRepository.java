package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkBookingInfoRepository extends JpaRepository<ParkBookingInfo,Long>,ParkBookingInfoRepositoryCustom {

    Page<ParkBookingInfo> findAllByUserIdOrderByStartTimeDesc(Long id, Pageable pageable);

    List<ParkBookingInfo> findAllByParkInfoIdAndUserId(Long parkInfoId, Long userId);

    Optional<ParkBookingInfo> findTopByParkInfoIdAndCarNumAndStartTimeLessThanEqualAndExitTimeGreaterThan(Long parkInfoId, String carNum, LocalDateTime startTime, LocalDateTime exitTime);

    @Query(value = "SELECT p from ParkBookingInfo p WHERE p.parkInfo.id = :parkId AND p.carNum = :carNum " +
            "AND ((p.startTime >= :startTime and p.startTime < :endTime) OR (p.exitTime > :startTime and p.exitTime <= :endTime))")
    ParkBookingInfo getAlreadyBookingInfo(@Param("parkId") Long parkId, @Param("carNum") String carNum, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    ParkBookingInfo findTopByParkInfoIdAndCarNumAndStartTimeGreaterThan(Long id, String carNum, LocalDateTime now);

    List<ParkBookingInfo> findAllByParkInfoIdAndExitTimeBetweenOrderByStartTimeDesc(Long parkInfoId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
