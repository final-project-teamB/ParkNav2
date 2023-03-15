package com.sparta.parknav.ticket.repository;

import com.sparta.parknav.ticket.entity.ParkBookingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkBookingInfoRepository extends JpaRepository<ParkBookingInfo,Long> {
    List<ParkBookingInfo> findAllByParkInfoId(Long parkId);
}
