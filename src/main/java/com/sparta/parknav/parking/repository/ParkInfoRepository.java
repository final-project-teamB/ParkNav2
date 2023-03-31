package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkInfoRepository extends JpaRepository<ParkInfo,Long>, ParkInfoRepositoryCustom{

    List<ParkInfo> findAllByIdBetween(Long firstParkInfoId, Long lastParkInfoId);

    List<ParkInfo> findByNameContains(String keyword);

}

