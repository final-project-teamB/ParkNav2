package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkInfoRepository extends JpaRepository<ParkInfo,Long>, ParkInfoRepositoryCustom{

    List<ParkInfo> findAllByIdBetween(Long firstParkInfoId, Long lastParkInfoId);

    @Query(value = "SELECT * FROM park_info a WHERE MATCH(a.name) AGAINST(?1 IN BOOLEAN MODE)", nativeQuery = true)
    List<ParkInfo> findAllParkInfoWithKeyword(@Param("matchKeyword") String matchKeyword);
}

