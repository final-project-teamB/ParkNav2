package com.sparta.parknav.management.entity;

import lombok.Getter;

@Getter
public enum ZoneType {
    GENERAL("일반"),
    BOOKING("예약"),
    COMMON("공통");

    private final String value;

    ZoneType(String value) {
        this.value = value;
    }

}
