package com.sparta.parknav.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ParkMgtListResponseDto {
    private Page page;
    private String parkName;

    @Builder
    private ParkMgtListResponseDto(Page page, String parkName) {
        this.parkName = parkName;
        this.page = page;
    }

    public static ParkMgtListResponseDto of(Page page, String parkName) {
        return builder()
                .page(page)
                .parkName(parkName)
                .build();
    }
}
