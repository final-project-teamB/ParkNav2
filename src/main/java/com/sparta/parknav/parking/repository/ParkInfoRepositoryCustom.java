package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkInfoRepositoryCustom {
    List<ParkOperInfo> findParkInfoWithOperInfoAndTypeQueryDsl(@Param("x") String x, @Param("y") String y, @Param("distance") double distance, @Param("type") String type);
}
