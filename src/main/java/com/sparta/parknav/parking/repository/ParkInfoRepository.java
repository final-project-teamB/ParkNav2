package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkInfoRepository extends JpaRepository<ParkInfo,Long> {

    @Query(value = "SELECT p FROM ParkInfo p WHERE ST_Distance_Sphere(Point(:x, :y), Point(p.lo, p  .la)) < :distance ORDER BY ST_Distance_Sphere(Point(:x, :y), Point(p.lo, p.la)) ASC")
    List<ParkInfo> findByParkDistance(@Param("x") String x, @Param("y") String y, @Param("distance") int distance);

}

