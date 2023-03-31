package com.sparta.parknav.parking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkOperRequestDto {

    private Long parkInfoId;
    private int parktime;
    private int charge;
}
