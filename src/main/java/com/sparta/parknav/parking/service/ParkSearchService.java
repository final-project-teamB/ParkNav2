package com.sparta.parknav.parking.service;

import com.sparta.parknav.global.data.KakaoMapService;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.MsgType;
import com.sparta.parknav.global.response.ResponseUtils;
import com.sparta.parknav.parking.dto.*;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkSearchService {

    private final KakaoMapService kakaoMapService;
    private final ParkInfoRepository parkInfoRepository;

    public ApiResponseDto<List<ParkOperInfoDto>> searchPark(ParkSearchRequestDto parkSearchRequestDto) {
        String lo,la;
//        List<ParkSearchResponseDto> parkSearchResponseDtos = new ArrayList<>();
        List<ParkOperInfoDto> parkOperInfoDtos = new ArrayList<>();
        //검색 키워드가 Null이 아닐경우는 키워드로 검색 이외에는 현위치 기반 검색
        if (parkSearchRequestDto.getKeyword() != null) {
            //카카오 검색 API호출
            KakaoSearchDto kakaoSearchDto = kakaoMapService.getKakaoSearch(parkSearchRequestDto.getKeyword());
            //결과가 없을경우 리턴
            if(kakaoSearchDto.getMeta().getTotal_count()==0){
                return ResponseUtils.ok(parkOperInfoDtos, MsgType.SEARCH_SUCCESSFULLY);
            }
            List<KakaoSearchDocumentsDto> kakaoSearchDocumentsDto = kakaoSearchDto.getDocuments();
            lo = kakaoSearchDocumentsDto.get(0).getX();
            la = kakaoSearchDocumentsDto.get(0).getY();
        } else {
            lo = parkSearchRequestDto.getLo();
            la = parkSearchRequestDto.getLa();
        }
        
        //lo,la 값을 기준으로 주변 3키로미터 이내의 주차장 검색
        List<Object[]> result = parkInfoRepository.findParkInfoWithOperInfo(lo,la,3000);

        for (Object[] row : result) {
            parkOperInfoDtos.add(ParkOperInfoDto.of((ParkOperInfo) row[0],(ParkInfo) row[1]));
        }

        return ResponseUtils.ok(parkOperInfoDtos, MsgType.SEARCH_SUCCESSFULLY);

    }

}
