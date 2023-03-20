package com.sparta.parknav.management.controller;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav._global.security.UserDetailsImpl;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.management.dto.request.CarNumDeleteRequestDto;
import com.sparta.parknav.management.dto.request.CarRegist;
import com.sparta.parknav.management.dto.response.CarListResponseDto;
import com.sparta.parknav.management.service.CarRegService;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

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

    @DeleteMapping(value = "/reg")
    public ApiResponseDto<Void> deletecar(@AuthenticationPrincipal UserDetailsImpl userDetails , @RequestBody CarNumDeleteRequestDto carNumDeleteRequestDto) {
        return carRegService.carDelete(carNumDeleteRequestDto,userDetails.getUser());
    }

    @GetMapping(value = "/check")
    public ApiResponseDto<List<CarListResponseDto>> carlist(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return carRegService.carlist(userDetails.getUser());
    }
}
