package com.sparta.parknav.parking.dto;

import lombok.Getter;

@Getter
public class ParkOperRequestDto {

    private Long parkInfoId;
    private int parktime;
    private int charge;
}
