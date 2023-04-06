package com.sparta.parknav.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginRequestDto {

    private String userId;
    private String password;

    @Builder
    private LoginRequestDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public static LoginRequestDto of (String userId, String password){
        return builder()
                .userId(userId)
                .password(password)
                .build();
    }
}
