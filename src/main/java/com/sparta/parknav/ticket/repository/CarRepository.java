package com.sparta.parknav.ticket.repository;

import com.sparta.parknav.ticket.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {

}
