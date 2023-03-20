package com.sparta.parknav.management.dto.response;

import com.sparta.parknav.booking.entity.Car;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CarListResponseDto {
    private String carNum;

    @Builder
    private CarListResponseDto(Car car) {
        this.carNum = car.getCarNum();
    }

    public static CarListResponseDto of(Car car){
        return builder()
                .car(car)
                .build();
    }
}

