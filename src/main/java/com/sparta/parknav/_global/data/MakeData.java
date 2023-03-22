package com.sparta.parknav._global.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.entity.ParkBookingInfo;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.booking.repository.ParkBookingInfoRepository;
import com.sparta.parknav.management.entity.ParkMgtInfo;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.ParkingFeeCalculator;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MakeData {

    private final KakaoMapService kakaoMapService;

    private final ParkInfoRepository parkInfoRepository;
    private final ParkOperInfoRepository parkOperInfoRepository;
    private final ParkBookingInfoRepository parkBookingInfoRepository;
    private final ParkMgtInfoRepository parkMgtInfoRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    @Value("${json.file.park-info}")
    private String parkInfoFilePath;

    @Value("${json.file.oper-info}")
    private String operInfoFilePath;

    @Transactional
    public void makeJsonToDatabase(String parkInfoFilePath, String operInfoFilePath) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File parkInfoFile = new File(parkInfoFilePath);
        List<ParkInfoJsonDto> parkInfos = objectMapper.readValue(parkInfoFile, new TypeReference<>() {
        });

        File OperInfoFile = new File(operInfoFilePath);
        List<ParkOperInfoJsonDto> operInfos = objectMapper.readValue(OperInfoFile, new TypeReference<>() {
        });

        int idx = 0;
        for (ParkInfoJsonDto parkInfoJson : parkInfos) {
            String name = parkInfoJson.getName();
            String address1 = parkInfoJson.getAddress1();
            String address2 = parkInfoJson.getAddress2();
            String la = parkInfoJson.getLa();
            String lo = parkInfoJson.getLo();

//            // 주소로 위도, 경도 정보 찾기
//            String address = address1.equals("") ? address2 : address1;
//            if (address.equals(""))
//                continue;
//            Map<String, Double> coordinates = kakaoMapService.getCoordinates(address1);
//            if (coordinates != null) {
//                double latitude = coordinates.get("latitude");
//                double longitude = coordinates.get("longitude");
//                la = String.valueOf(latitude);
//                lo = String.valueOf(longitude);
//            }

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

    @Transactional
    public ApiResponseDto<Void> makeBookingInfoData(Long firstParkInfoId, Long lastParkInfoId) {
        for (Long j = firstParkInfoId; j <= lastParkInfoId; j++) {
            // 주차장별 50개 랜덤 데이터 만들기
            for (int i = 1; i <= 50; i++) {
                // 예약 시작시간 랜덤 설정
                LocalDateTime start = LocalDateTime.of(2023, 3, 21, 9, 0, 0);
                LocalDateTime end = LocalDateTime.of(2023, 3, 22, 12, 0, 0);
                Duration duration = Duration.between(start, end);
                long hours = duration.toHours();
                // 예약 시작 시간
                LocalDateTime startTime = start.plusHours(ThreadLocalRandom.current()
                                .nextLong(hours + 1))
                        .truncatedTo(ChronoUnit.HOURS);
                // 예약 종료 시간
                LocalDateTime endTime = startTime.plusDays(2);

                User user = userRepository.getReferenceById((long) i);

                ParkInfo parkInfo = parkInfoRepository.getReferenceById(j);

                Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).get();

                // ParkBookingInfo 만들어서 저장
                ParkBookingInfo parkBookingInfo = ParkBookingInfo.of(startTime, endTime, user, parkInfo, car.getCarNum());
                parkBookingInfoRepository.save(parkBookingInfo);
            }
        }
        return ResponseUtils.ok(MsgType.BOOKING_SUCCESSFULLY);

    }


    // 현황관리 정보를 만든다. bookingInfo = null인 경우
    public ApiResponseDto<Void> makeMgtInfoData(Long firstParkInfoId, Long lastParkInfoId) {
        for (Long j = firstParkInfoId; j <= lastParkInfoId; j++) {
            ParkInfo parkInfo = parkInfoRepository.getReferenceById(j);
            ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(j).get();

            for (int i = 51; i <= 100; i++) {

                User user = userRepository.getReferenceById((long) i);

                Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).get();

                // 입차 시간 랜덤 설정
                LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
                LocalDateTime end = LocalDateTime.of(2023, 3, 15, 23, 59, 59);
                Duration duration = Duration.between(start, end);
                long hours = duration.toHours();
                LocalDateTime startTime = start.plusHours(ThreadLocalRandom.current()
                                .nextLong(hours + 1))
                        .truncatedTo(ChronoUnit.HOURS);
                // 출차 시간
                LocalDateTime endTime = startTime.plusHours(3);

                int charge = ParkingFeeCalculator.calculateParkingFee(Duration.between(startTime, endTime).toMinutes(), parkOperInfo);

                ParkMgtInfo mgtInfo = ParkMgtInfo.of(parkInfo, car.getCarNum(), startTime, endTime, charge, null);
                parkMgtInfoRepository.save(mgtInfo);
            }
        }
        return ResponseUtils.ok(MsgType.BOOKING_SUCCESSFULLY);
    }

    // MakeData Bean 이 생성될 떄, init() 함수가 실행된다.
    // dev 환경(MySQL)에서 실행한다면 주석 처리 후 실행할 것
//    @PostConstruct
    public void init() {
        try {
            // parameter에 파일 경로를 넣는다.
            makeJsonToDatabase(parkInfoFilePath, operInfoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // parkInfoId에 해당하는 주차장의 예약정보를 만든다.
//    @PostConstruct
//    public void initBooking() {
//        for (int parkInfoId = 6; parkInfoId <= 500; parkInfoId++) {
//            makeBookingInfoData((long) parkInfoId);
//        }
//    }

}
