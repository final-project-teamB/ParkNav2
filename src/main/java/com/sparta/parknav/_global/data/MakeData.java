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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
        // 주차장 정보 미리 로딩
        List<ParkInfo> parkInfoList = parkInfoRepository.findAllByIdBetween(firstParkInfoId, lastParkInfoId);
        // 유저 정보 미리 로딩
        List<User> userList = userRepository.findAllByIdBetween(1L, 50L);

        int cnt = 0;
        for (ParkInfo parkInfo : parkInfoList) {
            // 주차장별 50개 랜덤 데이터 만들기
            for (User user : userList) {
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

                Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).get();

                // ParkBookingInfo 만들어서 저장
                ParkBookingInfo parkBookingInfo = ParkBookingInfo.of(startTime, endTime, user, parkInfo, car.getCarNum());
                parkBookingInfoRepository.save(parkBookingInfo);

                if (cnt++ % 100 == 0) {
                    parkBookingInfoRepository.flush();
                }
            }
        }
        return ResponseUtils.ok(MsgType.DATA_SUCCESSFULLY);

    }

    // 현황관리 정보를 만든다. bookingInfo = null인 경우
    @Transactional
    public ApiResponseDto<Void> makeMgtInfoData(Long firstParkInfoId, Long lastParkInfoId) {
        // 주차장 정보 미리 로딩
        List<ParkInfo> parkInfoList = parkInfoRepository.findAllByIdBetween(firstParkInfoId, lastParkInfoId);
        // 주차장 운영정보 미리 로딩
        List<ParkOperInfo> parkOperInfoList = parkOperInfoRepository.findAllByParkInfoIdBetween(firstParkInfoId, lastParkInfoId);
        // 유저 정보 미리 로딩
        List<User> userList = userRepository.findAllByIdBetween(51L, 100L);
        // 쿼리문 줄이기 위한 List 생성
        List<ParkMgtInfo> mgtInfoList = new ArrayList<>();

        int idx = 0;
        for (ParkInfo parkInfo : parkInfoList) {

            ParkOperInfo parkOperInfo = parkOperInfoList.get(idx++);

            for (User user : userList) {

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
                mgtInfoList.add(mgtInfo);
            }
        }
        parkMgtInfoRepository.saveAll(mgtInfoList);

        return ResponseUtils.ok(MsgType.DATA_SUCCESSFULLY);
    }

    // 예약정보가 있는 현황관리 데이터 생성(예약한 차량이 입차한 경우)
    @Transactional
    public ApiResponseDto<Void> makeMgtBookingInfoData(Long firstParkInfoId, Long lastParkInfoId) {
        // 주차장 정보 미리 로딩
        List<ParkInfo> parkInfoList = parkInfoRepository.findAllByIdBetween(firstParkInfoId, lastParkInfoId);
        // 주차장 운영정보 미리 로딩
        List<ParkOperInfo> parkOperInfoList = parkOperInfoRepository.findAllByParkInfoIdBetween(firstParkInfoId, lastParkInfoId);
        // 유저 정보 미리 로딩
        List<User> userList = userRepository.findAllByIdBetween(1L, 40L);
        // 쿼리문 줄이기 위한 List 생성
        List<ParkMgtInfo> mgtInfoList = new ArrayList<>();

        int idx = 0;
        for (ParkInfo parkInfo : parkInfoList) {

            ParkOperInfo parkOperInfo = parkOperInfoList.get(idx++);

            for (User user : userList) {
                ParkBookingInfo bookingInfo = parkBookingInfoRepository.findAllByParkInfoAndUser(parkInfo, user).get(0);
                Car car = carRepository.findByUserIdAndIsUsingIs(user.getId(), true).get();

                // 예약시간 전후 30분 구하기
                Long randomMinute1 = new Random().nextInt(61) - 30L;
                Long randomMinute2 = new Random().nextInt(61) - 30L;
                // 입차시간은 예약시작시간 전후 30분으로 설정
                LocalDateTime enterTime = bookingInfo.getStartTime().plusMinutes(randomMinute1);
                LocalDateTime exitTime = null;
                // 출차일이 3/24 전이면, 출차시간은 예약 종료 일시 전후 30분으로 설정
                if (bookingInfo.getEndTime().isBefore(LocalDateTime.of(2023, 3, 24, 0, 0, 0))) {
                    exitTime = bookingInfo.getEndTime().plusMinutes(randomMinute2);
                }

                // 아직 주차중이면 요금은 0
                int charge = exitTime != null ? ParkingFeeCalculator.calculateParkingFee(Duration.between(enterTime, exitTime).toMinutes(), parkOperInfo) : 0;

                // 예약정보 포함하여 저장
                ParkMgtInfo mgtInfo = ParkMgtInfo.of(parkInfo, car.getCarNum(), enterTime, exitTime, charge, bookingInfo);
                mgtInfoList.add(mgtInfo);
            }
        }
        parkMgtInfoRepository.saveAll(mgtInfoList);

        return ResponseUtils.ok(MsgType.DATA_SUCCESSFULLY);
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

}
