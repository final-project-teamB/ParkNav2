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
    private int charge;

    private String parkInfoName;

    @Builder
    private ParkMgtResponseDto(String carNum, LocalDateTime enterTime,
                               LocalDateTime exitTime, int charge, String parkInfoName) {

        this.carNum = carNum;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.charge = charge;
        this.parkInfoName = parkInfoName;
    }

    public static ParkMgtResponseDto of(String carNum, LocalDateTime enterTime, LocalDateTime exitTime, int charge,String parkInfoName) {
        return builder()
                .carNum(carNum)
                .enterTime(enterTime)
                .exitTime(exitTime)
                .charge(charge)
                .parkInfoName(parkInfoName)
                .build();
    }
}
