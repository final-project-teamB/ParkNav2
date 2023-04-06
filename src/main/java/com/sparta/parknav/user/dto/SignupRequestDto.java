package com.sparta.parknav.user.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class SignupRequestDto {

    @NotBlank(message = "필수 정보입니다.")
    @Pattern(regexp = "^[a-z0-9]{5,20}$", message = "아이디는 5~20자의 영문 소문자, 숫자만 사용 가능합니다.")
    private String userId;

    @NotBlank(message = "필수 정보입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9~!@#$%^&*()_+=?,./<>{}\\[\\]\\-]{8,16}$", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @Builder
    private SignupRequestDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public static SignupRequestDto of (String userId, String password){
        return builder()
                .userId(userId)
                .password(password)
                .build();
    }
}
