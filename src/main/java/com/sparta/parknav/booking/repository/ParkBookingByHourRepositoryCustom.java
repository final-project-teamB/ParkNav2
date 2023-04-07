package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.ParkBookingByHour;

import java.time.LocalDateTime;
import java.util.List;

public interface ParkBookingByHourRepositoryCustom {
    List<ParkBookingByHour> findByParkInfoIdAndFromStartDateToEndDate(Long parkInfoId, LocalDateTime startDate, LocalDateTime endDate);
}
