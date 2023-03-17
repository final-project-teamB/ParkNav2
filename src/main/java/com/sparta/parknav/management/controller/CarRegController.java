package com.sparta.parknav.management.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.security.UserDetailsImpl;
import com.sparta.parknav.management.dto.request.CarRegist;
import com.sparta.parknav.management.service.CarRegService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarRegController {

    private final CarRegService carRegService;

    @PostMapping("/reg")
    public ApiResponseDto<Void> regist(@AuthenticationPrincipal UserDetailsImpl userDetails , @RequestBody CarRegist carRegist) {
        return carRegService.regist(userDetails.getUser(),carRegist);
    }

    @PutMapping("rep")
    public ApiResponseDto<MsgType> representative(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CarRegist carRegist) {
        return carRegService.representative(userDetails.getUser(), carRegist);
    }
}
