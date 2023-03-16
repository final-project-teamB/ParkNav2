package com.sparta.parknav.booking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BookingResponseDto {

    private Long bookingId;

    @Builder
    private BookingResponseDto(Long bookingId) {
        this.bookingId = bookingId;
    }

    public static BookingResponseDto of(Long bookingId) {
        return builder()
                .bookingId(bookingId)
                .build();
    }
}
