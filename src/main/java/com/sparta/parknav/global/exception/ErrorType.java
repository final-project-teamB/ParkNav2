package com.sparta.parknav.global.exception;

import lombok.Getter;

@Getter
public enum ErrorType {

    NOT_VALID_TOKEN(401, "토큰이 유효하지 않습니다."),
    NOT_TOKEN(401, "토큰이 없습니다."),
    INVALID_ARGUMENT(400, "비어있는 항목이 있습니다."),
    NOT_FOUND_USER(401, "등록된 사용자가 없습니다."),
    DUPLICATED_USERID(400, "중복된 아이디입니다."),
    NOT_MATCHING_INFO(401, "아이디 또는 비밀번호를 잘못 입력했습니다."),
    NOT_FOUND_CAR(400, "등록된 차량 번호가 없습니다."),
    NOT_FOUND_PARK(400, "등록된 주차장이 없습니다."),
    NOT_PARKING_SPACE(400, "주차할 공간이 없습니다."),
    NOT_ADMIN_USER(400, "관리자가 아닙니다.")
    ;
    private int code;
    private String msg;

    ErrorType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
