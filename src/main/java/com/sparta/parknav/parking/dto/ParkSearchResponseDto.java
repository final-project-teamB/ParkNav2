package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ParkSearchResponseDto {

    private String la;
    private String lo;
    private String placeName;
    private List<ParkOperInfoDto> parkOperInfoDtos;

    @Builder
    public ParkSearchResponseDto(String la, String lo,String placeName, List<ParkOperInfoDto> parkOperInfoDtos) {
        this.la = la;
        this.lo = lo;
        this.placeName = placeName;
        this.parkOperInfoDtos = parkOperInfoDtos;
    }

    public static ParkSearchResponseDto of(String la, String lo, String placeName,List<ParkOperInfoDto> parkOperInfoDtos){
        return builder()
                .la(la)
                .lo(lo)
                .placeName(placeName)
                .parkOperInfoDtos(parkOperInfoDtos)
                .build();
    }
}