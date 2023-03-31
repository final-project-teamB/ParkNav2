package com.sparta.parknav.parking.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.parking.dto.ParkOperInfoDto;
import com.sparta.parknav.parking.dto.ParkOperRequestDto;
import com.sparta.parknav.parking.dto.ParkSearchRequestDto;
import com.sparta.parknav.parking.dto.ParkSearchResponseDto;
import com.sparta.parknav.parking.service.ParkSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ParkSearchController {

    private final ParkSearchService parkSearchService;

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    @ResponseBody
    @GetMapping("/api/parks")
    public ApiResponseDto<ParkSearchResponseDto> searchPark(ParkSearchRequestDto parkSearchRequestDto) {

        return ResponseUtils.ok(parkSearchService.searchPark(parkSearchRequestDto), MsgType.SEARCH_SUCCESSFULLY);
    }

    @ResponseBody
    @GetMapping("/api/parks/oper-info")
    public ApiResponseDto<ParkOperInfoDto> OperationInfos(ParkOperRequestDto parkOperRequestDto) {

        return ResponseUtils.ok(parkSearchService.OperationInfos(parkOperRequestDto), MsgType.AVAILABLE_SUCCESSFULLY);
    }
}
