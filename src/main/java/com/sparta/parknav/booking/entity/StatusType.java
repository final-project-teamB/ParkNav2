package com.sparta.parknav.booking.entity;

public enum StatusType {
    USED("사용완료"),
    UNUSED("미사용"),
    EXPIRED("기간만료");

    private final String value;

    StatusType(String value) {
        this.value = value;
    }

}
