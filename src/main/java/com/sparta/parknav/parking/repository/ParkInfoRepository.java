package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkInfoRepository extends JpaRepository<ParkInfo,Long> {

    @Query(value = "SELECT p FROM ParkInfo p WHERE ST_Distance_Sphere(Point(:x, :y), Point(p.lo, p  .la)) < :distance ORDER BY ST_Distance_Sphere(Point(:x, :y), Point(p.lo, p.la)) ASC")
    List<ParkInfo> findByParkDistance(@Param("x") String x, @Param("y") String y, @Param("distance") int distance);
    @Query(value = "SELECT p FROM ParkOperInfo p JOIN p.parkInfo WHERE ST_Distance_Sphere(Point(:x, :y), Point(p.parkInfo.lo, p.parkInfo.la)) < :distance ORDER BY ST_Distance_Sphere(Point(:x, :y), Point(p.parkInfo.lo, p.parkInfo.la)) ASC")
    List<ParkOperInfo> findParkInfoWithOperInfo(@Param("x") String x, @Param("y") String y, @Param("distance") double distance);
    @Query(value = "SELECT p FROM ParkOperInfo p JOIN p.parkInfo WHERE ST_Distance_Sphere(Point(:x, :y), Point(p.parkInfo.lo, p.parkInfo.la)) < :distance and p.parkCtgy=:type ORDER BY ST_Distance_Sphere(Point(:x, :y), Point(p.parkInfo.lo, p.parkInfo.la)) ASC")
    List<ParkOperInfo> findParkInfoWithOperInfoAndType(@Param("x") String x, @Param("y") String y, @Param("distance") double distance, @Param("type") String type);

    List<ParkInfo> findAllByIdBetween(Long firstParkInfoId, Long lastParkInfoId);

}

