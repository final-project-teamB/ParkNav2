package com.sparta.parknav.parking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkSearchRequestDto {

    private String keyword;
    private int parktime;
    private int charge;
    private int type;
    private boolean available;
    private String la;
    private String lo;

}
