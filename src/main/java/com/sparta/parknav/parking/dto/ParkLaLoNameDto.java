package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkLaLoNameDto {

    private Long parkInfoId;
    private String la;
    private String lo;
    private String name;

    @Builder
    private ParkLaLoNameDto(ParkOperInfo parkOperInfo) {
        parkInfoId = parkOperInfo.getParkInfo().getId();
        la = parkOperInfo.getParkInfo().getLa();
        lo = parkOperInfo.getParkInfo().getLo();
        name = parkOperInfo.getParkInfo().getName();
    }

    public static ParkLaLoNameDto of(ParkOperInfo park) {
        return builder()
                .parkOperInfo(park)
                .build();
    }
}
