package com.sparta.parknav._global.data;

import com.sparta.parknav._global.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data")
public class MakeDataController {

    private final MakeData makeData;

    @GetMapping("/{firstParkInfoId}/{lastParkInfoId}")
    public ApiResponseDto<Void> getInfoBeforeBooking(@PathVariable Long firstParkInfoId,@PathVariable Long lastParkInfoId) {
        return makeData.makeBookingInfoData(firstParkInfoId, lastParkInfoId);
    }
}
