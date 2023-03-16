package com.sparta.parknav.booking.repository;

import com.sparta.parknav.booking.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {

}
