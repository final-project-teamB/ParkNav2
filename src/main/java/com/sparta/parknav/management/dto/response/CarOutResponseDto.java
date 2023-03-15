package com.sparta.parknav.management.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CarOutResponseDto {

    private String charge;
    private LocalDateTime exitTime;
}
