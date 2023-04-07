package com.sparta.parknav.management.repository;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkMgtInfoRepository extends JpaRepository<ParkMgtInfo, Long> {

    //    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    List<ParkMgtInfo> findAllByParkInfoId(Long parkId);

    Page<ParkMgtInfo> findAllByParkInfoIdOrderByEnterTimeDesc(Long parkId, Pageable pageable);

    int countByParkInfoIdAndExitTimeIsNull(Long id);

    Optional<ParkMgtInfo> findTopByParkInfoIdAndCarNumAndExitTimeNullOrderByEnterTimeDesc(Long parkId, String carNum);

    Optional<ParkMgtInfo> findByParkBookingInfoId(Long id);

    int countByParkBookingInfoIn(List<ParkBookingInfo> bookingInfoList);

    Boolean existsByParkBookingInfoIdAndExitTimeIsNotNull(Long bookingInfoId);

    // @EntityGraph 어노테이션을 사용하여 fetch join을 적용
    // ParkMgtInfo와 함께 ParkBookingInfo 엔티티도 함께 조회할 수 있게 됨
    @EntityGraph(attributePaths = {"parkBookingInfo"})
    List<ParkMgtInfo> findAllByExitTimeIsNullAndParkBookingInfoEndTimeBefore(@NotNull LocalDateTime now);

}
