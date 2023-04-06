package com.sparta.parknav._global.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public enum ErrorType {

    NOT_VALID_TOKEN(401, "토큰이 유효하지 않습니다."),
    NOT_TOKEN(401, "토큰이 없습니다."),
    INVALID_ARGUMENT(400, "비어있는 항목이 있습니다."),
    NOT_FOUND_USER(401, "등록된 사용자가 없습니다."),
    DUPLICATED_USERID(400, "중복된 아이디입니다."),
    NOT_MATCHING_INFO(401, "아이디 또는 비밀번호를 잘못 입력했습니다."),
    NOT_FOUND_PARK_TYPE(400, "유효하지 않은 값입니다."),
    NOT_FOUND_PARK(400, "등록된 주차장이 없습니다."),
    NOT_PARKING_SPACE(400, "주차할 공간이 없습니다."),
    ALREADY_TAKEN_OUT_CAR(400,"이미 출차된 차량입니다."),
    NOT_AVAILABLE_TIME(400, "예약 시작 시간은 현재 시간 이전일 수 없습니다."),
    NOT_END_TO_START(400,"입차시간이 출차시간보다 빨라야 합니다."),
    NOT_FOUND_CAR(400, "등록된 차량이 없습니다."),
    NOT_FOUND_BOOKING(400, "예약 내역이 존재하지 않습니다."),
    NOT_BOOKING_USER(400, "본인이 예약한 내역이 아닙니다."),
    NOT_MGT_USER(400, "해당 주차장의 관리자가 아닙니다."),
    ALREADY_REG_CAR(400, "이미 등록된 차량입니다."),
    ALREADY_ENTER_CAR(400, "이미 입차된 차량입니다."),
    ALREADY_REG_REP_CAR(400, "이미 대표로 등록된 차량입니다."),
    ALREADY_RESERVED(400, "이미 예약된 시간입니다."),
    FAILED_TO_ACQUIRE_LOCK(100, "락 권한을 얻는데 실패했습니다."),
    NOT_ALLOWED_BOOKING_TIME(400, "예약불가한 시간이 포함되어 있습니다."),
    NOT_FOUND_PARK_OPER_INFO(400, "해당 주차장 운영정보가 없습니다."),
    NOT_OPEN_SELECTED_DATE(400, "선택하신 시간에는 운영하지 않습니다."),
    CONTENT_IS_NULL(400,"입력되지 않은 정보가 있습니다."),
    INTERRUPTED_WHILE_WAITING_FOR_LOCK(400, "락을 기다리는 동안 인터럽트가 발생하였습니다");

    private int code;
    private String msg;

    ErrorType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ErrorType printLocalDateTimeList(List<LocalDateTime> notAllowedTimeList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String> dateTimeStrings = notAllowedTimeList.stream()
                .map(dateTime -> dateTime.format(formatter))
                .toList();

        ErrorType errorType = NOT_ALLOWED_BOOKING_TIME;
        errorType.msg = dateTimeStrings.toString();

        return errorType;
    }
}
