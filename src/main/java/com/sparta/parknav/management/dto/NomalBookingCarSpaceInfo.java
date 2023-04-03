package com.sparta.parknav.management.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NomalBookingCarSpaceInfo {
    private int NomalCarSpace;

    private int BookingCarSpace;


    @Builder
    private NomalBookingCarSpaceInfo(int nomalCarSpace, int bookingCarSpace) {
        NomalCarSpace = nomalCarSpace;
        BookingCarSpace = bookingCarSpace;
    }

    public static NomalBookingCarSpaceInfo of (int nomalCarSpace, int bookingCarSpace){
        return builder()
                .nomalCarSpace(nomalCarSpace)
                .bookingCarSpace(bookingCarSpace)
                .build();
    }
}

