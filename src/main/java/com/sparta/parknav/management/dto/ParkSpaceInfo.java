package com.sparta.parknav.management.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkSpaceInfo {

    private int generalCarSpace;
    private int bookingCarSpace;
    private int commonCarSpace;

    @Builder
    private ParkSpaceInfo(int generalCarSpace, int bookingCarSpace, int commonCarSpace) {
        this.generalCarSpace = generalCarSpace;
        this.bookingCarSpace = bookingCarSpace;
        this.commonCarSpace = commonCarSpace;
    }

    public static ParkSpaceInfo of (int generalCarSpace, int bookingCarSpace, int commonCarSpace){
        return builder()
                .generalCarSpace(generalCarSpace)
                .bookingCarSpace(bookingCarSpace)
                .commonCarSpace(commonCarSpace)
                .build();
    }
}

