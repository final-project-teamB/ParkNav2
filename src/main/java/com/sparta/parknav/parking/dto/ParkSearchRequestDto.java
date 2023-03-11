package com.sparta.parknav.parking.dto;

import lombok.Getter;

@Getter
public class ParkSearchRequestDto {

    private int parktime;

    private int charge;

    private int type;

    private int available;

    private String keyword;

}
