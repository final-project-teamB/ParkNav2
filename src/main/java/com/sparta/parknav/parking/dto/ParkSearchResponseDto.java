package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.Builder;

public class ParkSearchResponseDto {

    private Long id;

    private String la;

    private String lo;

    @Builder
    private ParkSearchResponseDto(ParkInfo parkInfo) {
        this.id = parkInfo.getId();
        this.la = parkInfo.getLa();
        this.lo = parkInfo.getLo();

    }

    public static ParkSearchResponseDto from(ParkInfo parkInfo){
        return ParkSearchResponseDto.builder()
                .parkInfo(parkInfo)
                .build();
    }

}
