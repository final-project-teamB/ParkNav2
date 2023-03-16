package com.sparta.parknav.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class CarInResponseDto {

    private String carNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterTime;

    @Builder
    private CarInResponseDto(String carNum, LocalDateTime enterTime) {
        this.carNum = carNum;
        this.enterTime = enterTime;
    }

    public static CarInResponseDto of(String carNum,LocalDateTime enterTime) {
        return builder()
                .carNum(carNum)
                .enterTime(enterTime)
                .build();
    }
}
