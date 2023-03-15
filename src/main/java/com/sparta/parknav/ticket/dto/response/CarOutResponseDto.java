package com.sparta.parknav.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CarOutResponseDto {

    private String charge;
    private LocalDateTime exitTime;
}
