package com.sparta.parknav.global.response;

import lombok.Getter;

@Getter
public enum MsgType {

    SIGNUP_SUCCESSFULLY("회원가입이 완료되었습니다."),
    LOGIN_SUCCESSFULLY("로그인이 완료되었습니다."),
    SEARCH_SUCCESSFULLY("조회 성공")
    ;

    private final String msg;

    MsgType(String msg) {
        this.msg = msg;
    }
}
