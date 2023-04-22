package com.sparta.parknav.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class CarOutResponseDto {

    private int charge;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitTime;

    @Builder
    private CarOutResponseDto(int charge, LocalDateTime exitTime) {
        this.charge = charge;
        this.exitTime = exitTime;
    }

    public static CarOutResponseDto of(int charge, LocalDateTime exitTime) {
        return builder()
                .charge(charge)
                .exitTime(exitTime)
                .build();
    }
}
