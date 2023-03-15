package com.sparta.parknav.global.response;

import lombok.Getter;

@Getter
public enum MsgType {

    SIGNUP_SUCCESSFULLY("회원가입이 완료되었습니다."),
    LOGIN_SUCCESSFULLY("로그인이 완료되었습니다."),
    SEARCH_SUCCESSFULLY("조회 성공"),
    ENTER_SUCCESSFULLY("입차 완료"),
    EXIT_SUCCESSFULLY("출차 완료")
    ;

    private final String msg;

    MsgType(String msg) {
        this.msg = msg;
    }
}
