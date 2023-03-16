package com.sparta.parknav.booking.controller;

import com.sparta.parknav.booking.Service.BookingService;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.global.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{park-id}")
    public ApiResponseDto<BookingInfoResponseDto> getInfoBeforeBooking(@PathVariable(name = "park-id") Long id, BookingInfoRequestDto requestDto) {
        return bookingService.getInfoBeforeBooking(id, requestDto);
    }

}
