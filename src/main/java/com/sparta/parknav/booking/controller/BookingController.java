package com.sparta.parknav.booking.controller;

import com.sparta.parknav.booking.Service.BookingService;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{park-id}")
    public ApiResponseDto<BookingInfoResponseDto> getInfoBeforeBooking(@PathVariable(name = "park-id") Long id, BookingInfoRequestDto requestDto) {
        return bookingService.getInfoBeforeBooking(id, requestDto);
    }

    @PostMapping("/{park-id}")
    public ApiResponseDto<Void> bookingPark(@PathVariable(name = "park-id") Long id,
                                            @RequestBody BookingInfoRequestDto requestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return bookingService.bookingPark(id, requestDto, userDetails.getUser());
    }

}
