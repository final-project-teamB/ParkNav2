package com.sparta.parknav.parking.repository;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ParkOperInfoRepository extends JpaRepository<ParkOperInfo, Long> {

    // LOCKING
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkOperInfo> findByParkInfoId(Long parkInfoId);

    List<ParkOperInfo> findAllByParkInfoIdBetween(Long firstParkInfoId, Long lastParkInfoId);
}
