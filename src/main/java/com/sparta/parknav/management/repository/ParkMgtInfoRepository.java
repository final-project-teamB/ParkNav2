package com.sparta.parknav.management.repository;

import com.sparta.parknav.management.entity.ParkMgtInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo,Long> {
    List<ParkMgtInfo> findAllByParkInfoId(Long parkId);
    Page<ParkMgtInfo> findAllByParkInfoId(Long parkId, Pageable pageable);
    int countByParkInfoIdAndExitTimeIsNull(Long id);

    Optional<ParkMgtInfo> findTopByParkInfoIdAndCarNumOrderByEnterTimeDesc(Long parkId, String carNum);
}