package com.sparta.parknav.users.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String usersId;

    @Builder
    private LoginResponseDto(String usersId) {
        this.usersId = usersId;
    }

    public static LoginResponseDto from(String usersId) {
        return LoginResponseDto.builder()
                .usersId(usersId)
                .build();
    }
}
