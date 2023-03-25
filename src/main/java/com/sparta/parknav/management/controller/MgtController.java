package com.sparta.parknav.management.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav._global.security.AdminDetailsImpl;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtListResponseDto;
import com.sparta.parknav.management.service.MgtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/mgt")
@RequiredArgsConstructor
public class MgtController {

    private final MgtService mgtService;

    @PostMapping("/enter")
    public ApiResponseDto<CarInResponseDto> enter(@Valid @RequestBody CarNumRequestDto requestDto, @AuthenticationPrincipal AdminDetailsImpl adminDetails) {
        return ResponseUtils.ok(mgtService.enter(requestDto, adminDetails.getUser()), MsgType.ENTER_SUCCESSFULLY);
    }

    @PutMapping("/exit")
    public ApiResponseDto<CarOutResponseDto> exit(@Valid @RequestBody CarNumRequestDto requestDto, @AuthenticationPrincipal AdminDetailsImpl adminDetails) {
        return ResponseUtils.ok(mgtService.exit(requestDto, adminDetails.getUser()), MsgType.EXIT_SUCCESSFULLY);
    }

    @GetMapping("/check")
    public ApiResponseDto<ParkMgtListResponseDto> mgtPage(@AuthenticationPrincipal AdminDetailsImpl userDetails,
                                                          @RequestParam int page,
                                                          @RequestParam int size) {
        return ResponseUtils.ok(mgtService.mgtPage(userDetails.getUser(), page, size), MsgType.SEARCH_SUCCESSFULLY);
    }
}
