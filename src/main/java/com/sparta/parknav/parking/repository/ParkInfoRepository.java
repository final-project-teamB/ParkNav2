package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkInfoRepository extends JpaRepository<ParkInfo, Long>, ParkInfoRepositoryCustom {

    List<ParkInfo> findAllByIdBetween(Long firstParkInfoId, Long lastParkInfoId);

    List<ParkInfo> findByNameContains(String keyword);

}

