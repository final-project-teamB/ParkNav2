package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByUserIdAndIsUsingIs(Long userId, Boolean isUsing);

}
