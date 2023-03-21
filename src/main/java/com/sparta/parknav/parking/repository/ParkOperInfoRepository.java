package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkOperInfoRepository extends JpaRepository<ParkOperInfo, Long> {

    Optional<ParkOperInfo> findByParkInfoId(Long parkInfoId);

}
