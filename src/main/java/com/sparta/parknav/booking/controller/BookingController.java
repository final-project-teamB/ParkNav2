package com.sparta.parknav.booking.controller;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav._global.security.UserDetailsImpl;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.dto.BookingInfoResponseDto;
import com.sparta.parknav.booking.dto.BookingResponseDto;
import com.sparta.parknav.booking.dto.MyBookingResponseDto;
import com.sparta.parknav.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{park-id}")
    public ApiResponseDto<BookingInfoResponseDto> getInfoBeforeBooking(@PathVariable(name = "park-id") Long id, BookingInfoRequestDto requestDto) {
        return ResponseUtils.ok(bookingService.getInfoBeforeBooking(id, requestDto), MsgType.SEARCH_SUCCESSFULLY);
    }

    @PostMapping("/{park-id}")
    public ApiResponseDto<BookingResponseDto> bookingPark(@PathVariable(name = "park-id") Long parkId,
                                                          @RequestBody BookingInfoRequestDto requestDto,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseUtils.ok(bookingService.bookingPark(parkId, requestDto, userDetails.getUser()), MsgType.BOOKING_SUCCESSFULLY);
    }

    @DeleteMapping("/{booking-id}")
    public ApiResponseDto<Void> cancelBooking(@PathVariable(name = "booking-id") Long id,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseUtils.ok(MsgType.CANCEL_SUCCESSFULLY);
    }

    @GetMapping("/mypage")
    public ApiResponseDto<Page<MyBookingResponseDto>> getMyBooking(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                   @RequestParam int page,
                                                                   @RequestParam int size) {
        return ResponseUtils.ok(bookingService.getMyBooking(userDetails.getUser(),page, size), MsgType.SEARCH_SUCCESSFULLY);
    }

}
