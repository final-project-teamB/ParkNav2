package com.sparta.parknav.ticket.dto.response;

import com.sparta.parknav.ticket.entity.Car;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
public class CarInResponseDto {

    private String carNum;
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
