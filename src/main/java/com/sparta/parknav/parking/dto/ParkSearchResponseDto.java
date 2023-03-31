package com.sparta.parknav.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ParkSearchResponseDto {

    private String la;
    private String lo;
    private String placeName;
    private List<ParkLaLoNameDto> parkOperInfoDtos;

    @Builder
    public ParkSearchResponseDto(String la, String lo, String placeName, List<ParkLaLoNameDto> parkOperInfoDtos) {
        this.la = la;
        this.lo = lo;
        this.placeName = placeName;
        this.parkOperInfoDtos = parkOperInfoDtos;
    }

    public static ParkSearchResponseDto of(String la, String lo, String placeName, List<ParkLaLoNameDto> parkOperInfoDtos) {
        return builder()
                .la(la)
                .lo(lo)
                .placeName(placeName)
                .parkOperInfoDtos(parkOperInfoDtos)
                .build();
    }
}
