package com.sparta.parknav.parking.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class KakaoSearchDto {
    private List<KakaoSearchDocumentsDto> documents;
    private KakaoSearchMetaDto meta;
}
