package com.sparta.parknav.parking.service;

import com.sparta.parknav._global.data.KakaoMapService;
import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav.booking.service.OperationChecking;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.dto.*;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.entity.ParkType;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkSearchService {

    private final KakaoMapService kakaoMapService;
    private final ParkInfoRepository parkInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    public ParkSearchResponseDto searchPark(ParkSearchRequestDto parkSearchRequestDto) {
        String lo = null, la = null, placeName = null;
        int similarScore = 100;
        int searchSimilarScore = 100;
        List<ParkLaLoNameDto> parkInfoDtos = new ArrayList<>();

        //검색 키워드가 Null이 아닐경우는 키워드로 검색 이외에는 현위치 기반 검색
        if (parkSearchRequestDto.getKeyword() != null) {
            //카카오 검색 API호출
            KakaoSearchDto kakaoSearchDto = kakaoMapService.getKakaoSearch(parkSearchRequestDto.getKeyword());
            if (kakaoSearchDto.getMeta().getTotal_count() > 0) {
                List<KakaoSearchDocumentsDto> kakaoSearchDocumentsDto = kakaoSearchDto.getDocuments();
                lo = kakaoSearchDocumentsDto.get(0).getX();
                la = kakaoSearchDocumentsDto.get(0).getY();
                placeName = kakaoSearchDocumentsDto.get(0).getPlace_name();
                // 0번 결과로 유사도 초기화
                similarScore = similarKeyword(parkSearchRequestDto.getKeyword(), kakaoSearchDocumentsDto.get(0).getPlace_name());
                for (KakaoSearchDocumentsDto kakao : kakaoSearchDocumentsDto) {
                    // 검색결과 별 유사도 비교
                    searchSimilarScore = similarKeyword(parkSearchRequestDto.getKeyword(), kakao.getPlace_name());
                    if (searchSimilarScore < similarScore) {
                        lo = kakao.getX();
                        la = kakao.getY();
                        placeName = kakao.getPlace_name();
                        similarScore = searchSimilarScore;
                    }
                }
            }
            // DB에서 Like검색
            List<ParkInfo> parkInfos = parkInfoRepository.findByNameContains(parkSearchRequestDto.getKeyword());
            for (ParkInfo parkInfo : parkInfos) {
                // 검색결과 별 유사도 비교
                searchSimilarScore = similarKeyword(parkSearchRequestDto.getKeyword(), parkInfo.getName());
                if (searchSimilarScore < similarScore) {
                    la = parkInfo.getLa();
                    lo = parkInfo.getLo();
                    placeName = parkInfo.getName();
                    similarScore = searchSimilarScore;
                }
            }
            //결과가 없을경우 리턴
            if (placeName == null) {
                return null;
            }
        } else {
            lo = parkSearchRequestDto.getLo();
            la = parkSearchRequestDto.getLa();
            placeName = "내 위치";
        }

        //lo,la 값을 기준으로 주변 3키로미터 이내의 주차장 검색
        List<ParkOperInfo> result;
        //주차장 유형에 따라 쿼리를 다르게 지정


        result = parkInfoRepository.findParkInfoWithOperInfoAndTypeQueryDsl(lo, la, 2, ParkType.fromValue(parkSearchRequestDto.getType()));

        for (ParkOperInfo park : result) {

            ParkLaLoNameDto parkLaLoNameDto = ParkLaLoNameDto.of(park);
            if (ParkingFeeCalculator.calculateParkingFee(parkSearchRequestDto.getParktime() * 60L, park) <= parkSearchRequestDto.getCharge()) {
                parkInfoDtos.add(parkLaLoNameDto);
            }
        }
        return ParkSearchResponseDto.of(la, lo, placeName, parkInfoDtos);
    }

    @Transactional
    public ParkOperInfoDto OperationInfos(ParkOperRequestDto parkOperRequestDto) {

        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(parkOperRequestDto.getParkInfoId()).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_PARK_OPER_INFO)
        );

        String available;
        // 현재 운영여부 확인
        if (OperationChecking.checkOperation(LocalDateTime.now(), parkOperInfo)) {
            // 현재 주차 가능 대수 = 주차 가능 대수 - 출차시간이 없는 현황 수(주차중인 경우)
            available = (parkOperInfo.getCmprtCo() - parkMgtInfoRepository.countByParkInfoIdAndExitTimeIsNull(parkOperInfo.getParkInfo().getId())) + "대";
        } else {
            // 운영중이 아니라면 메시지 출력
            available = MsgType.NOT_OPEN_NOW.getMsg();
        }

        return ParkOperInfoDto.of(parkOperInfo, ParkingFeeCalculator.calculateParkingFee(parkOperRequestDto.getParktime() * 60L, parkOperInfo), available);
    }

    public static int similarKeyword(String userKeyword, String keyword) {
        return LevenshteinDistance.getDefaultInstance().apply(userKeyword, keyword);
    }
}
