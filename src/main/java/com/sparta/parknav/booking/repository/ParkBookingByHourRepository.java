package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.ParkBookingByHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ParkBookingByHourRepository extends JpaRepository<ParkBookingByHour, Long> {

    List<ParkBookingByHour> findByParkInfoIdAndDateBetweenAndAvailableEquals (Long parkInfoId, LocalDate startDate, LocalDate endDate, int available);

    Optional<ParkBookingByHour> findByParkInfoIdAndDateAndTime (Long parkInfoId, LocalDate startDate, int time);
}
