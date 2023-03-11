package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkMgtInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo,Long> {
}
