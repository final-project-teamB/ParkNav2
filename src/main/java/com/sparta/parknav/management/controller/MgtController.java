package com.sparta.parknav.management.controller;

import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.security.AdminDetailsImpl;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.management.service.MgtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mgt")
@RequiredArgsConstructor
public class MgtController {

    private final MgtService mgtService;

    @PostMapping("/enter")
    public ApiResponseDto<CarInResponseDto> enter(@RequestBody CarNumRequestDto requestDto) {
        return mgtService.enter(requestDto);
    }

    @PutMapping("/exit")
    public ApiResponseDto<CarOutResponseDto> exit(@RequestBody CarNumRequestDto requestDto) {
        return mgtService.exit(requestDto);
    }

    @GetMapping("/check")
    public ApiResponseDto<List<ParkMgtResponseDto>> mgtPage(@AuthenticationPrincipal AdminDetailsImpl userDetails) {
        return mgtService.mgtPage(userDetails.getUser());
    }
}
