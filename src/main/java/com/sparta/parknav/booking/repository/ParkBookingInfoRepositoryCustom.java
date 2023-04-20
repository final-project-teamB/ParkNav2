package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.dto.ParkBookingInfoMgtDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkBookingInfoRepositoryCustom {

    Page<ParkBookingInfoMgtDto> findByMgtList(Long parkInfoId, int state, int sort, Pageable pageable);
}
