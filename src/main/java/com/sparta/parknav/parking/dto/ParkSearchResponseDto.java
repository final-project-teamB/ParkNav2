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
    public ParkSearchResponseDto(String la, String lo, String placeName, List<ParkLaLoNameDto> parkLaLoNameDtos) {
        this.la = la;
        this.lo = lo;
        this.placeName = placeName;
        this.parkOperInfoDtos = parkLaLoNameDtos;
    }

    public static ParkSearchResponseDto of(String la, String lo, String placeName, List<ParkLaLoNameDto> parkLaLoNameDtos) {
        return builder()
                .la(la)
                .lo(lo)
                .placeName(placeName)
                .parkLaLoNameDtos(parkLaLoNameDtos)
                .build();
    }
}
