package com.sparta.parknav.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BookingInfoResponseDto {

    private List<LocalDateTime> notAllowedTimeList;
    private Integer charge;
    private Boolean isOperation;

    @Builder
    private BookingInfoResponseDto(List<LocalDateTime> notAllowedTimeList, Integer charge, Boolean isOperation) {
        this.notAllowedTimeList = notAllowedTimeList;
        this.charge = charge;
        this.isOperation = isOperation;
    }

    public static BookingInfoResponseDto of(List<LocalDateTime> notAllowedTimeList, Integer charge, Boolean isOperation) {
        return BookingInfoResponseDto.builder()
                .notAllowedTimeList(notAllowedTimeList)
                .charge(charge)
                .isOperation(isOperation)
                .build();
    }

    public static BookingInfoResponseDto of(Boolean isOperation) {
        return BookingInfoResponseDto.builder()
                .isOperation(isOperation)
                .build();
    }
}
