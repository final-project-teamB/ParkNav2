package com.sparta.parknav.parking.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav.parking.dto.ParkSearchResponseDto;
import com.sparta.parknav.parking.dto.ParkSearchRequestDto;
import com.sparta.parknav.parking.service.ParkSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ParkSearchController {

    private final ParkSearchService parkSearchService;

    @GetMapping("/main")
    public String main(){
        return "main";
    }

    @ResponseBody
    @GetMapping("/api/parks")
    public ApiResponseDto<List<ParkSearchResponseDto>> serchPark(ParkSearchRequestDto parkSearchRequestDto){

        return parkSearchService.searchPark(parkSearchRequestDto);

    }

}
