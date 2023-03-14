package com.sparta.parknav.global.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MakeData {

    private final KakaoMapService kakaoMapService;

    private final ParkInfoRepository parkInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;

    @Value("${json.file.park-info}")
    private String parkInfoFilePath;

    @Value("${json.file.oper-info}")
    private String operInfoFilePath;

    public void makeJsonToDatabase(String parkInfoFilePath, String operInfoFilePath) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File parkInfoFile = new File(parkInfoFilePath);
        List<ParkInfoJsonDto> parkInfos = objectMapper.readValue(parkInfoFile, new TypeReference<>() {
        });

        File OperInfoFile = new File(operInfoFilePath);
        List<ParkOperInfoJsonDto> operInfos = objectMapper.readValue(OperInfoFile, new TypeReference<>() {
        });

        int idx = 0;
        for(ParkInfoJsonDto parkInfoJson : parkInfos) {
            String name = parkInfoJson.getName();
            String address1 = parkInfoJson.getAddress1();
            String address2 = parkInfoJson.getAddress2();
            String la = "";
            String lo = "";

            // 주소로 위도, 경도 정보 찾기
            Map<String, Double> coordinates = kakaoMapService.getCoordinates(address1);
            if (coordinates != null) {
                double latitude = coordinates.get("latitude");
                double longitude = coordinates.get("longitude");
                la = String.valueOf(latitude);
                lo = String.valueOf(longitude);
            }

            ParkInfo parkInfo = ParkInfo.of(name, address1, address2, la, lo);
            parkInfoRepository.save(parkInfo);

            // ParkOperInfo 객체 만들기
            ParkOperInfo parkOperInfo = ParkOperInfo.of(parkInfo, parkInfoJson.getParking_type());

            // ParkOperInfoJsonDto에서 필드 값 가져오기
            String weekdayOpen = operInfos.get(idx).getWeekdayOpen();
            String weekdayClose = operInfos.get(idx).getWeekdayClose();
            String satOpen = operInfos.get(idx).getSatOpen();
            String satClose = operInfos.get(idx).getSatClose();
            String sunOpen = operInfos.get(idx).getSunOpen();
            String sunClose = operInfos.get(idx).getSunClose();
            int chargeBsTime = Integer.parseInt(operInfos.get(idx).getChargeBsTime());
            int chargeBsChrg = Integer.parseInt(operInfos.get(idx).getChargeBsChrg());
            int chargeAditUnitTime = Integer.parseInt(operInfos.get(idx).getChargeAditUnitTime());
            int chargeAditUnitChrg = Integer.parseInt(operInfos.get(idx).getChargeAditUnitChrg());
            int cmprtCo = Integer.parseInt(operInfos.get(idx).getCmprtCo());
            idx++;

            // ParkOperInfo 객체에 데이터 넣고 저장하기
            parkOperInfo.update(weekdayOpen, weekdayClose, satOpen, satClose, sunOpen, sunClose, chargeBsTime, chargeBsChrg, chargeAditUnitTime, chargeAditUnitChrg, cmprtCo);
            parkOperInfoRepository.save(parkOperInfo);

        }
    }

    // MakeData Bean 이 생성될 떄, init() 함수가 실행된다.
    // dev 환경(MySQL)에서 실행한다면 주석 처리 후 실행할 것
//    @PostConstruct
//    public void init() {
//        try {
//            // parameter에 파일 경로를 넣는다.
//            makeJsonToDatabase(parkInfoFilePath, operInfoFilePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
