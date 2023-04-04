package com.sparta.parknav.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.parknav.management.entity.ZoneType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CarInResponseDto {

    private String carNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterTime;
    private String zone;

    @Builder
    private CarInResponseDto(String carNum, LocalDateTime enterTime, String zone) {
        this.carNum = carNum;
        this.enterTime = enterTime;
        this.zone = zone;
    }

    public static CarInResponseDto of(String carNum,LocalDateTime enterTime, ZoneType zone) {
        return builder()
                .carNum(carNum)
                .enterTime(enterTime)
                .zone(zone.getValue())
                .build();
    }
}
