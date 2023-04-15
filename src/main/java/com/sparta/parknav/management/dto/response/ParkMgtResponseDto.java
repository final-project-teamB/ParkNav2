package com.sparta.parknav.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ParkMgtResponseDto {

    private String carNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingEndTime;
    private int charge;

    @Builder
    private ParkMgtResponseDto(String carNum, LocalDateTime enterTime,
                               LocalDateTime exitTime, LocalDateTime bookingStartTime, LocalDateTime bookingEndTime, int charge) {

        this.carNum = carNum;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.bookingStartTime = bookingStartTime;
        this.bookingEndTime = bookingEndTime;
        this.charge = charge;
    }

    public static ParkMgtResponseDto of(String carNum, LocalDateTime enterTime, LocalDateTime exitTime, LocalDateTime startTime, LocalDateTime endTime, int charge) {
        return builder()
                .carNum(carNum)
                .enterTime(enterTime)
                .exitTime(exitTime)
                .bookingStartTime(startTime)
                .bookingEndTime(endTime)
                .charge(charge)
                .build();
    }
}
