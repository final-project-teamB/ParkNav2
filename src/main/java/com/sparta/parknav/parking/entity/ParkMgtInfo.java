package com.sparta.parknav.parking.entity;

import com.sparta.parknav.ticket.entity.Car;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkMgtInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_info_id")
    private ParkInfo parkInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(nullable = false)
    private LocalDateTime enterTime;

    @Column(nullable = false)
    private LocalDateTime exitTime;

    @Column(nullable = false)
    private int charge;
}
