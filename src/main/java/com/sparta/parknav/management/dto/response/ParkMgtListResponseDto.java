package com.sparta.parknav.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ParkMgtListResponseDto {
    private Page page;
    private String parkName;

    private Long parkId;

    private int totalActualCharge;
    private int totalEstimatedCharge;

    @Builder
    private ParkMgtListResponseDto(Page page, String parkName, Long parkId, int totalActualCharge, int totalEstimatedCharge) {
        this.parkName = parkName;
        this.page = page;
        this.parkId = parkId;
        this.totalActualCharge = totalActualCharge;
        this.totalEstimatedCharge = totalEstimatedCharge;
    }

    public static ParkMgtListResponseDto of(Page page, String parkName, Long parkId, int totalActualCharge, int totalEstimatedCharge) {
        return builder()
                .page(page)
                .parkName(parkName)
                .parkId(parkId)
                .totalActualCharge(totalActualCharge)
                .totalEstimatedCharge(totalEstimatedCharge)
                .build();
    }

}
