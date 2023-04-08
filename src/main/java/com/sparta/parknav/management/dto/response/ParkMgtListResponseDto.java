package com.sparta.parknav.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ParkMgtListResponseDto {
    private Page page;
    private String parkName;

    private Long parkId;

    @Builder
    private ParkMgtListResponseDto(Page page, String parkName, Long parkId) {
        this.parkName = parkName;
        this.page = page;
        this.parkId = parkId;
    }

    public static ParkMgtListResponseDto of(Page page, String parkName, Long parkId) {
        return builder()
                .page(page)
                .parkName(parkName)
                .parkId(parkId)
                .build();
    }
}
