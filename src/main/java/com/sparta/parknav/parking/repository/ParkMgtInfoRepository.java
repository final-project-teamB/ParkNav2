package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkMgtInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo,Long> {
    List<ParkMgtInfo> findAllByParkInfoId(Long parkId);
    int countByParkInfoIdAndExitTimeIsNull(Long id);

    Optional<ParkMgtInfo> findByParkInfoIdAndCarNum(Long parkId, String carNum);
}
