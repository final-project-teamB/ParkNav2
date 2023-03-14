package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkSearchResponseDto {

    private Long id;
    private String la;
    private String lo;
    private String name;
    private String address1;
    private String address2;

    @Builder
    private ParkSearchResponseDto(ParkInfo parkInfo) {
        this.id = parkInfo.getId();
        this.la = parkInfo.getLa();
        this.lo = parkInfo.getLo();
        this.name = parkInfo.getName();
        this.address1 = parkInfo.getAddress1();
        this.address2 = parkInfo.getAddress2();

    }

    public static ParkSearchResponseDto from(ParkInfo parkInfo){
        return ParkSearchResponseDto.builder()
                .parkInfo(parkInfo)
                .build();
    }

}
