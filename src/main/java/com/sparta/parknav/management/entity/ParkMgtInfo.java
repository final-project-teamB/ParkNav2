package com.sparta.parknav.management.entity;

import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.AccessLevel;
import lombok.Builder;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_booking_info_id")
    private ParkBookingInfo parkBookingInfo;

    @Column(nullable = false)
    private LocalDateTime enterTime;

    @Column
    private LocalDateTime exitTime;

    @Column
    private Integer charge;

    @Column(nullable = false)
    private String carNum;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private ZoneType zone;

    @Builder
    private ParkMgtInfo(ParkInfo parkInfo, String carNum, LocalDateTime enterTime, LocalDateTime exitTime, int charge, ParkBookingInfo parkBookingInfo, ZoneType zone) {
        this.parkInfo = parkInfo;
        this.carNum = carNum;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.charge = charge;
        this.parkBookingInfo = parkBookingInfo;
        this.zone = zone;
    }

    public static ParkMgtInfo of(ParkInfo parkInfo, String carNum, LocalDateTime enterTime, LocalDateTime exitTime, int charge, ParkBookingInfo parkBookingInfo, ZoneType zone) {
        return builder()
                .parkInfo(parkInfo)
                .carNum(carNum)
                .enterTime(enterTime)
                .exitTime(exitTime)
                .charge(charge)
                .parkBookingInfo(parkBookingInfo)
                .zone(zone)
                .build();
    }

    public static ParkMgtInfo of(ParkInfo parkInfo, String carNum, LocalDateTime enterTime, int charge, ParkBookingInfo parkBookingInfo, ZoneType zone) {
        return builder()
                .parkInfo(parkInfo)
                .carNum(carNum)
                .enterTime(enterTime)
                .charge(charge)
                .parkBookingInfo(parkBookingInfo)
                .zone(zone)
                .build();
    }

    public static ParkMgtInfo of(ParkInfo parkInfo, String carNum, LocalDateTime enterTime, ZoneType zone) {
        return builder()
                .parkInfo(parkInfo)
                .carNum(carNum)
                .enterTime(enterTime)
                .zone(zone)
                .build();
    }

    public void update(int charge, LocalDateTime exitTime) {
        this.charge = charge;
        this.exitTime = exitTime;
    }

    public void updateZone(ZoneType zone){
        this.zone = zone;
    }
}
