package com.sparta.parknav.management.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.security.AdminDetailsImpl;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.dto.response.CarInResponseDto;
import com.sparta.parknav.management.dto.response.CarOutResponseDto;
import com.sparta.parknav.management.dto.response.ParkMgtResponseDto;
import com.sparta.parknav.management.service.MgtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mgt")
@RequiredArgsConstructor
public class MgtController {

    private final MgtService mgtService;

    @PostMapping("/enter")
    public ApiResponseDto<CarInResponseDto> enter(@Valid @RequestBody CarNumRequestDto requestDto, @AuthenticationPrincipal AdminDetailsImpl adminDetails) {
        return mgtService.enter(requestDto, adminDetails.getUser());
    }

    @PutMapping("/exit")
    public ApiResponseDto<CarOutResponseDto> exit(@Valid @RequestBody CarNumRequestDto requestDto, @AuthenticationPrincipal AdminDetailsImpl adminDetails) {
        return mgtService.exit(requestDto, adminDetails.getUser());
    }

    @GetMapping("/check")
    public ApiResponseDto<Page<ParkMgtResponseDto>> mgtPage(@AuthenticationPrincipal AdminDetailsImpl userDetails,
                                                            @RequestParam int page,
                                                            @RequestParam int size) {
        return mgtService.mgtPage(userDetails.getUser(), page-1, size);
    }
}
