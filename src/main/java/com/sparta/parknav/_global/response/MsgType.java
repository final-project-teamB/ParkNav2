package com.sparta.parknav._global.response;

import lombok.Getter;

@Getter
public enum MsgType {

    SIGNUP_SUCCESSFULLY("회원가입이 완료되었습니다."),
    LOGIN_SUCCESSFULLY("로그인이 완료되었습니다."),
    SEARCH_SUCCESSFULLY("조회 성공"),
    ENTER_SUCCESSFULLY("입차 완료"),
    EXIT_SUCCESSFULLY("출차 완료"),
    NOT_OPEN_NOW("현재 운영중이 아닙니다."),
    NOT_OPEN_SELECT_DATE("선택시간에는 운영하지 않습니다."),
    BOOKING_SUCCESSFULLY("예약이 완료되었습니다."),
    CANCEL_SUCCESSFULLY("예약이 취소되었습니다."),
    REGISTRATION_SUCCESSFULLY("차량 등록 성공"),
    REP_REG_SUCCESSFULLY("대표 차량 등록 성공"),
    REP_DEL_SUCCESSFULLY("차량 삭제 성공"),
    DATA_SUCCESSFULLY("데이터 생성 성공"),
    AVAILABLE_SUCCESSFULLY("운영 정보 조회 성공"),
    ;

    private final String msg;

    MsgType(String msg) {
        this.msg = msg;
    }
}
