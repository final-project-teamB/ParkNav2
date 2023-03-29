package com.sparta.parknav.booking.service;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class OperationChecking {

    private LocalTime openTime;
    private LocalTime closeTime;

    @Builder
    private OperationChecking(LocalTime openTime, LocalTime closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public static OperationChecking of(LocalTime openTime, LocalTime closeTime) {
        return builder()
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
    }

    public static boolean checkOperation(LocalDateTime selectedTime, ParkOperInfo parkOperInfo) {

        OperationChecking operation = OperationChecking.of(null, null);

        // selectedTime 의 요일에 따른 운영시간 체크
        switch(selectedTime.getDayOfWeek()) {
            case SATURDAY -> {
                operation.openTime = LocalTime.parse(parkOperInfo.getSatOpen());
                operation.closeTime = LocalTime.parse(parkOperInfo.getSatClose());
            }
            case SUNDAY -> {
                operation.openTime = LocalTime.parse(parkOperInfo.getSunOpen());
                operation.closeTime = LocalTime.parse(parkOperInfo.getSunClose());
            }
            default -> {
                operation.openTime = LocalTime.parse(parkOperInfo.getWeekdayOpen());
                operation.closeTime = LocalTime.parse(parkOperInfo.getWeekdayClose());
            }
        }

        // 시작시간, 종료시간이 00:00시라면 해당일엔 운영하지 않음
        if (operation.openTime.equals(LocalTime.of(0, 0)) && operation.closeTime.equals(LocalTime.of(0, 0))) {
            return false;
        }

        // selectedTime 이 운영시작시간 전이거나 운영종료시간 후인 경우 -> 선택한 시간은 운영 안함
        if (selectedTime.toLocalTime().isBefore(operation.openTime) || (!operation.closeTime.equals(LocalTime.of(0, 0)) && selectedTime.toLocalTime().isAfter(operation.closeTime))) {
            return false;
        }

        return true;
    }
}
