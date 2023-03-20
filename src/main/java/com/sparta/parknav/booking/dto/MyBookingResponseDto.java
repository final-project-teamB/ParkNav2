package com.sparta.parknav.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.entity.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyBookingResponseDto {

    private String parkName;

    private String carNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;

    private Integer charge;

    private StatusType status;

    @Builder
    private MyBookingResponseDto(String parkName, String carNum, LocalDateTime startDate, LocalDateTime endDate, Integer charge, StatusType status) {
        this.parkName = parkName;
        this.carNum = carNum;
        this.startDate = startDate;
        this.endDate = endDate;
        this.charge = charge;
        this.status = status;
    }

    public static MyBookingResponseDto of(ParkBookingInfo parkBookingInfo, Integer charge, StatusType status) {
        return builder()
                .parkName(parkBookingInfo.getParkInfo().getName())
                .carNum(parkBookingInfo.getCarNum())
                .startDate(parkBookingInfo.getStartTime())
                .endDate(parkBookingInfo.getEndTime())
                .charge(charge)
                .status(status)
                .build();
    }

}
