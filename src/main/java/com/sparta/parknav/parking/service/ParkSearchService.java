package com.sparta.parknav.parking.service;

import com.sparta.parknav.global.data.KakaoMapService;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.MsgType;
import com.sparta.parknav.global.response.ResponseUtils;
import com.sparta.parknav.parking.dto.*;
import com.sparta.parknav.parking.entity.ParkInfo;
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

    public ApiResponseDto<List<ParkSearchResponseDto>> searchPark(ParkSearchRequestDto parkSearchRequestDto) {
        String lo, la;
        List<ParkSearchResponseDto> parkOperInfoDtos = new ArrayList<>();

        //검색 키워드가 Null이 아닐경우는 키워드로 검색 이외에는 현위치 기반 검색
        if (parkSearchRequestDto.getKeyword() != null) {
            //카카오 검색 API호출
            KakaoSearchDto kakaoSearchDto = kakaoMapService.getKakaoSearch(parkSearchRequestDto.getKeyword());
            //결과가 없을경우 리턴
            if (kakaoSearchDto.getMeta().getTotal_count() == 0) {
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
        List<Object[]> result ;
        if (parkSearchRequestDto.getType()==1) {
            result = parkInfoRepository.findParkInfoWithOperInfo(lo, la, 3000);
        }else{
            result = parkInfoRepository.findParkInfoWithOperInfoAndType(lo, la, 3000, ParkType.fromValue(parkSearchRequestDto.getType()));
        }
        for (Object[] row : result) {
            ParkSearchResponseDto parkOperInfoDto = calculateChrg((ParkOperInfo) row[0], (ParkInfo) row[1], parkSearchRequestDto.getParktime());
            if (parkOperInfoDto.getTotCharge() <= parkSearchRequestDto.getCharge()){
                parkOperInfoDtos.add(parkOperInfoDto);
            }
        }

        return ResponseUtils.ok(parkOperInfoDtos, MsgType.SEARCH_SUCCESSFULLY);

    }

    public ParkSearchResponseDto calculateChrg(ParkOperInfo parkOperInfo, ParkInfo parkInfo, int parktime) {
        int parkTimeMin = parktime * 60;

        //기본 요금만 있는 경우 ( 시간제한 x )
        if (parkOperInfo.getChargeBsTime() == 0 && parkOperInfo.getChargeAditUnitTime() == 0) {
            return ParkSearchResponseDto.of(parkOperInfo, parkInfo, parkOperInfo.getChargeBsChrg());
        }

        //기본 시간보다 적은경우
        if (parkTimeMin <= parkOperInfo.getChargeBsTime()) {
            return ParkSearchResponseDto.of(parkOperInfo, parkInfo, parkOperInfo.getChargeBsChrg());
        }

        //추가 시간이 없는 경우
        if (parkOperInfo.getChargeAditUnitTime() == 0) {
            return ParkSearchResponseDto.of(parkOperInfo, parkInfo, parkOperInfo.getChargeBsChrg());
        }

        return ParkSearchResponseDto.of(parkOperInfo, parkInfo, ((((parkTimeMin - parkOperInfo.getChargeBsTime()) / parkOperInfo.getChargeAditUnitTime())) * parkOperInfo.getChargeAditUnitChrg()) + parkOperInfo.getChargeBsChrg());
    }

}
