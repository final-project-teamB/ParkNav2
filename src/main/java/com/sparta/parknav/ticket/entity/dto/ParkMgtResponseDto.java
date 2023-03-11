package com.sparta.parknav.ticket.entity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ParkMgtResponseDto {

    private String carNum;
    private LocalDateTime enterTime;
    private LocalDateTime exitTime;
    private int charge;

    @Builder
    private ParkMgtResponseDto(String carNum, LocalDateTime enterTime,
                               LocalDateTime exitTime, int charge) {

        this.carNum = carNum;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.charge = charge;
    }
}
