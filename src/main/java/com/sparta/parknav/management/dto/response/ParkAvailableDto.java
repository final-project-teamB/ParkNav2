package com.sparta.parknav.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ParkAvailableDto {
    private LocalDate date;
    private int time;
    private int available;
    @Builder
    private ParkAvailableDto(LocalDate date, int time, int available) {
        this.date = date;
        this.time = time;
        this.available = available;
    }
    public static ParkAvailableDto of(LocalDate date, int time, int available) {
        return builder()
                .date(date)
                .time(time)
                .available(available)
                .build();
    }
}
