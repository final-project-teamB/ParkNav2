package com.sparta.parknav.booking.entity;

import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkBookingByHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer time;

    @Column(nullable = false)
    private Integer available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARK_INFO_ID", nullable = false)
    private ParkInfo parkInfo;

    @Builder
    private ParkBookingByHour(LocalDate date, Integer time, Integer available, ParkInfo parkInfo) {
        this.date = date;
        this.time = time;
        this.available = available;
        this.parkInfo = parkInfo;
    }

    public static ParkBookingByHour of(LocalDate date, Integer time, Integer available, ParkInfo parkInfo) {
        return builder()
                .date(date)
                .time(time)
                .available(available)
                .parkInfo(parkInfo)
                .build();
    }

    public void updateCnt(int cnt) {
        this.available = this.available + cnt;
    }
}
