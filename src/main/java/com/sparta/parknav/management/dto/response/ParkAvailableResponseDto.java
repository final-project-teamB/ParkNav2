package com.sparta.parknav.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ParkAvailableResponseDto {
    private int cmprtCo;
    private String parkName;
    private List<ParkAvailableDto> parkAvailableDtos;
    @Builder
    private ParkAvailableResponseDto(int cmprtCo, String parkName, List<ParkAvailableDto> parkAvailableDtos) {
        this.cmprtCo = cmprtCo;
        this.parkName = parkName;
        this.parkAvailableDtos = parkAvailableDtos;

    }
    public static ParkAvailableResponseDto of(int cmprtCo, String parkName, List<ParkAvailableDto> parkAvailableDtos) {
        return builder()
                .cmprtCo(cmprtCo)
                .parkName(parkName)
                .parkAvailableDtos(parkAvailableDtos)
                .build();
    }
}
