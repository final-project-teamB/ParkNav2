package com.sparta.parknav.management.controller;

import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.security.AdminDetailsImpl;
import com.sparta.parknav.global.security.UserDetailsImpl;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.management.service.MgtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mgt")
@RequiredArgsConstructor
public class MgtController {

    private final MgtService mgtService;

    @PostMapping("enter")
    public ApiResponseDto<CarInResponseDto> enter(@RequestBody CarNumRequestDto requestDto) {
        return mgtService.enter(requestDto);
    }

    @PutMapping("exit")
    public ApiResponseDto<CarOutResponseDto> exit(@AuthenticationPrincipal AdminDetailsImpl adminDetails, @RequestBody CarNumRequestDto requestDto) {
        return mgtService.exit(requestDto);
    }

    @GetMapping("page")
    public ApiResponseDto<ParkMgtResponseDto> mgtPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return mgtService.mgtPage(userDetails.getUser());
    }
}
