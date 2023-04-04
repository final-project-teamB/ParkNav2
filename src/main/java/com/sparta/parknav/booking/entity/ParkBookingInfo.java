package com.sparta.parknav.booking.entity;

import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkBookingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String carNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARK_INFO_ID", nullable = false)
    private ParkInfo parkInfo;


    @Builder
    private ParkBookingInfo(LocalDateTime startTime, LocalDateTime endTime, User user, ParkInfo parkInfo, String carNum) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.parkInfo = parkInfo;
        this.carNum =carNum;
    }

    public static ParkBookingInfo of(BookingInfoRequestDto requestDto, User user, ParkInfo parkInfo, String carNum) {
        return ParkBookingInfo.builder()
                .startTime(requestDto.getStartDate())
                .endTime(requestDto.getEndDate())
                .user(user)
                .parkInfo(parkInfo)
                .carNum(carNum)
                .build();
    }

    public static ParkBookingInfo of(LocalDateTime startTime, LocalDateTime endTime, User user, ParkInfo parkInfo, String carNum) {
        return ParkBookingInfo.builder()
                .startTime(startTime)
                .endTime(endTime)
                .user(user)
                .parkInfo(parkInfo)
                .carNum(carNum)
                .build();
    }

    public static ParkBookingInfo of(BookingInfoRequestDto requestDto, ParkInfo parkInfo, String carNum) {
        return ParkBookingInfo.builder()
                .startTime(requestDto.getStartDate())
                .endTime(requestDto.getEndDate())
                .parkInfo(parkInfo)
                .carNum(carNum)
                .build();
    }

}
