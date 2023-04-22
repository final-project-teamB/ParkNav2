package com.sparta.parknav.parking.entity;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;

public enum ParkType {
    ALL("전체"),
    PUBLIC_PARKING("공영"),
    PARKING("민영");

    private final String value;
    ParkType(String value) {
        this.value = value;
    }
    public static String fromValue(int value) {
        switch (value) {
            case 1:
                return ALL.value;
            case 2:
                return PUBLIC_PARKING.value;
            case 3:
                return PARKING.value;
            default:
                throw new CustomException(ErrorType.NOT_FOUND_PARK_TYPE);
        }
    }
}
