package com.sparta.parknav.booking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BookingInfoResponseDto {

    private String available;
    private String booking;
    private Integer charge;

    @Builder
    private BookingInfoResponseDto(String available, String booking, Integer charge) {
        this.available = available;
        this.booking = booking;
        this.charge = charge;
    }

    public static BookingInfoResponseDto of(String available, String booking, Integer charge) {
        return BookingInfoResponseDto.builder()
                .available(available)
                .booking(booking)
                .charge(charge)
                .build();
    }
}
