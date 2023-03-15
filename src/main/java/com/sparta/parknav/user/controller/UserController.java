package com.sparta.parknav.user.controller;

import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.user.dto.SignupRequestDto;
import com.sparta.parknav.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService usersService;

    @PostMapping("/signup")
    public ApiResponseDto<Void> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return usersService.signup(requestDto);
    }

}
