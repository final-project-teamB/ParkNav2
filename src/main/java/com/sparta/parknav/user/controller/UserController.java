package com.sparta.parknav.user.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.user.dto.LoginRequestDto;
import com.sparta.parknav.user.dto.SignupRequestDto;
import com.sparta.parknav.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @PostMapping("/users/signup")
    public ApiResponseDto<Void> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseUtils.ok(MsgType.SIGNUP_SUCCESSFULLY);
    }

    @PostMapping("/users/login")
    public ApiResponseDto<Void> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        userService.login(requestDto, response);
        return ResponseUtils.ok(MsgType.LOGIN_SUCCESSFULLY);
    }

    @PostMapping("/admins/login")
    public ApiResponseDto<Void> loginAdmin(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        userService.loginAdmin(requestDto, response);
        return ResponseUtils.ok(MsgType.LOGIN_SUCCESSFULLY);

    }

}
