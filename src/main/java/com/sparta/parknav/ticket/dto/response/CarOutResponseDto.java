package com.sparta.parknav.ticket.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CarOutResponseDto {

    private String charge;
    private LocalDateTime exitTime;
}
