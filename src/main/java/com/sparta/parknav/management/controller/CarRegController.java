package com.sparta.parknav.management.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.security.UserDetailsImpl;
import com.sparta.parknav.management.dto.request.CarRegist;
import com.sparta.parknav.management.dto.response.CarListResponseDto;
import com.sparta.parknav.management.service.CarRegService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarRegController {

    private final CarRegService carRegService;

    @PostMapping("/reg")
    public ApiResponseDto<Void> regist(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CarRegist carRegist) {
        return carRegService.regist(userDetails.getUser(), carRegist);
    }

    @PutMapping(value = "/rep")
    public ApiResponseDto<Void> representative(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CarRegist carRegist) {
        return carRegService.representative(userDetails.getUser(), carRegist);
    }

    @GetMapping(value = "/check")
    public ApiResponseDto<List<CarListResponseDto>> carlist(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return carRegService.carlist(userDetails.getUser());
    }
}
