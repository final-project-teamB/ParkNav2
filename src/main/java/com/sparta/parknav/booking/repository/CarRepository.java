package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByUserIdAndIsUsingIs(Long userId, Boolean isUsing);

    Car findByUserAndIsUsingIs(User user, Boolean isUsing);

    boolean existsByUser(User user);

    Car findByUserAndCarNum(User user, String carNum);

    List<Car> findByUserIdOrderByIsUsingDesc(Long userId);

    Optional<Car> findFirstByUserIdAndIsUsingFalse(Long userId);
}
