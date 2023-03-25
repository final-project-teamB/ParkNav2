package com.sparta.parknav.parking.service;

import com.sparta.parknav._global.data.KakaoMapService;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.dto.*;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.entity.ParkType;
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

    public ParkSearchResponseDto searchPark(ParkSearchRequestDto parkSearchRequestDto) {
        String lo, la, placeName;
        List<ParkOperInfoDto> parkOperInfoDtos = new ArrayList<>();
        ParkSearchResponseDto parkSearchResponseDto = null;

        //검색 키워드가 Null이 아닐경우는 키워드로 검색 이외에는 현위치 기반 검색
        if (parkSearchRequestDto.getKeyword() != null) {
            //카카오 검색 API호출
            KakaoSearchDto kakaoSearchDto = kakaoMapService.getKakaoSearch(parkSearchRequestDto.getKeyword());
            //결과가 없을경우 리턴
            if (kakaoSearchDto.getMeta().getTotal_count() == 0) {
                return parkSearchResponseDto;
            }
            List<KakaoSearchDocumentsDto> kakaoSearchDocumentsDto = kakaoSearchDto.getDocuments();
            lo = kakaoSearchDocumentsDto.get(0).getX();
            la = kakaoSearchDocumentsDto.get(0).getY();
            placeName = kakaoSearchDocumentsDto.get(0).getPlace_name();
            for (KakaoSearchDocumentsDto kakao : kakaoSearchDocumentsDto) {
                if (kakao.getPlace_name().equals(parkSearchRequestDto.getKeyword())) {
                    lo = kakao.getX();
                    la = kakao.getY();
                    placeName = kakao.getPlace_name();
                    break;
                }
            }

        } else {
            lo = parkSearchRequestDto.getLo();
            la = parkSearchRequestDto.getLa();
            placeName = "내 위치";
        }

        //lo,la 값을 기준으로 주변 3키로미터 이내의 주차장 검색
        List<ParkOperInfo> result;
        //주차장 유형에 따라 쿼리를 다르게 지정
        if (parkSearchRequestDto.getType() == 1) {
            result = parkInfoRepository.findParkInfoWithOperInfo(lo, la, 3000);
        } else {
            result = parkInfoRepository.findParkInfoWithOperInfoAndType(lo, la, 3000, ParkType.fromValue(parkSearchRequestDto.getType()));
        }

        for (ParkOperInfo park:result){
            ParkOperInfoDto parkOperInfoDto = ParkOperInfoDto.of(park, ParkingFeeCalculator.calculateParkingFee(parkSearchRequestDto.getParktime()* 60L,park));
            if (parkOperInfoDto.getTotCharge() <= parkSearchRequestDto.getCharge()) {
                parkOperInfoDtos.add(parkOperInfoDto);
            }
        }
        return ParkSearchResponseDto.of(la, lo, placeName, parkOperInfoDtos);
    }
}
