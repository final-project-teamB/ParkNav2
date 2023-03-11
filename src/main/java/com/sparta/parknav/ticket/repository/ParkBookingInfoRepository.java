package com.sparta.parknav.ticket.repository;

import com.sparta.parknav.ticket.entity.ParkBookingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkBookingInfoRepository extends JpaRepository<ParkBookingInfo,Long> {
}
