package com.sparta.parknav.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ParkBookingInfoMgtDto {
    private String carNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingEndTime;
    private Long usersId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingExitTime;
    private Long MgtId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitTime;
    private int charge;

    public ParkBookingInfoMgtDto(String carNum, LocalDateTime bookingStartTime, LocalDateTime bookingEndTime, Long usersId, LocalDateTime bookingExitTime, Long mgtId, LocalDateTime enterTime, LocalDateTime exitTime, int charge) {
        this.carNum = carNum;
        this.bookingStartTime = bookingStartTime;
        this.bookingEndTime = bookingEndTime;
        this.usersId = usersId;
        this.bookingExitTime = bookingExitTime;
        this.MgtId = mgtId;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.charge = charge;
    }
}
