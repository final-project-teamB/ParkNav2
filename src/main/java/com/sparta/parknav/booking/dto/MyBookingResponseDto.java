package com.sparta.parknav.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.entity.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyBookingResponseDto {

    private Long bookingId;

    private Long parkId;

    private String parkName;

    private String carNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime enterTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime exitTime;

    private Integer charge;

    private StatusType status;

    @Builder
    private MyBookingResponseDto(Long bookingId, Long parkId, String parkName, String carNum, LocalDateTime startDate, LocalDateTime endDate, Integer charge, StatusType status, LocalDateTime enterTime, LocalDateTime exitTime) {
        this.bookingId = bookingId;
        this.parkId = parkId;
        this.parkName = parkName;
        this.carNum = carNum;
        this.startDate = startDate;
        this.endDate = endDate;
        this.charge = charge;
        this.status = status;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
    }

    public static MyBookingResponseDto of(ParkBookingInfo parkBookingInfo, Integer charge, StatusType status, LocalDateTime enterTime, LocalDateTime exitTime) {
        return builder()
                .bookingId(parkBookingInfo.getId())
                .parkId(parkBookingInfo.getParkInfo().getId())
                .parkName(parkBookingInfo.getParkInfo().getName())
                .carNum(parkBookingInfo.getCarNum())
                .startDate(parkBookingInfo.getStartTime())
                .endDate(parkBookingInfo.getEndTime())
                .charge(charge)
                .status(status)
                .enterTime(enterTime)
                .exitTime(exitTime)
                .build();
    }

}
