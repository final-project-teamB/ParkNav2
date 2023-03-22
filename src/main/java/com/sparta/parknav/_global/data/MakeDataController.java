package com.sparta.parknav._global.data;

import com.sparta.parknav._global.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data")
public class MakeDataController {

    private final MakeData makeData;

    @PostMapping("/{firstParkInfoId}/{lastParkInfoId}")
    public ApiResponseDto<Void> getInfoBeforeBooking(@PathVariable Long firstParkInfoId,@PathVariable Long lastParkInfoId) {
        return makeData.makeBookingInfoData(firstParkInfoId, lastParkInfoId);
    }

    @PostMapping("/mgtinfo/{firstParkInfoId}/{lastParkInfoId}")
    public ApiResponseDto<Void> makeMgtInfoData(@PathVariable Long firstParkInfoId,@PathVariable Long lastParkInfoId) {
        return makeData.makeMgtInfoData(firstParkInfoId, lastParkInfoId);
    }

}
