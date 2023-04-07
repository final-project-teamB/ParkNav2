package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.ParkBookingByHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ParkBookingByHourRepository extends JpaRepository<ParkBookingByHour, Long> {

    ParkBookingByHour findByParkInfoIdAndDateAndTime(Long parkInfoId, LocalDate startDate, int time);
}
