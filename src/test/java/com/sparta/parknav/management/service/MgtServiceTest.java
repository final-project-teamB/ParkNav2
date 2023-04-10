package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.Admin;
import com.sparta.parknav.user.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(properties = "spring.profiles.active:test")
public class MgtServiceTest {


    @Autowired
    private MgtService mgtService;
    @Autowired
    private ParkInfoRepository parkInfoRepository;
    @Autowired
    private ParkOperInfoRepository parkOperInfoRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Test
    public void NoBookingEnter() throws NoSuchFieldException, IllegalAccessException, InterruptedException {

        // Given
        log.info("enter 동시성 테스트 시작");
        // admin 객체를 생성하기 위한 parkInfo 생성
        ParkInfo parkInfo = parkInfoRepository.save(ParkInfo.of("테스트주차장", "테스트주소1", "테스트주소2", "33.2501489768202", "126.563230508718"));
        ParkOperInfo parkOperInfoTmp = ParkOperInfo.of(parkInfo, "민영");
        parkOperInfoTmp.update("00:00", "23:59", "00:00", "23:59", "00:00", "23:59", 30, 1000, 30, 500, 10);
        ParkOperInfo parkOperInfo = parkOperInfoRepository.save(parkOperInfoTmp);
        Admin admin = Admin.of("admin", "1234", parkInfo);
        adminRepository.save(admin);
        parkInfoRepository.flush();

        int numOfUsers = 20;
        // 대기하는 스레드의 숫자를 지정
        CountDownLatch endLatch = new CountDownLatch(numOfUsers);
        List<CarNumRequestDto> vehicleIds = generateVehicleIds(numOfUsers);
        // 20개의 스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(numOfUsers);
        // 원자적인 연산을 보장해주는 Atomic 패키지 클래스로 멀티 스레드 환경에서 여러 스레드들이 동시에 값을 변경할 수 없도록 안전성 보장
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // When
        log.info("enter 동시성 테스트 진행");
        for (CarNumRequestDto vehicleId : vehicleIds) {
            executorService.execute(() -> {
                try {
                    mgtService.enter(vehicleId, admin);
                    successCount.getAndIncrement();
                    log.info("입차 성공");
                    log.info("차량 아이디 : {}", vehicleId.getCarNum());
                } catch (CustomException e) {
                    System.out.println(e.getMessage());
                    failCount.getAndIncrement();
                    log.info("주차장 자리가 꽉찼습니다.");
                }
                //스레드가 끝나면 -1 을 해서 endLatch를 1 줄임.
                endLatch.countDown();
            });
        }

        // Then
        log.info("enter 동시성 테스트 결과 검증");
        // 모든 스레드들이 끝날때까지 대기. 모든 스레드들이 끝나면 다음 코드 실행, 즉 endLatch가 0이 되면 다음 코드 실행
        endLatch.await();
        log.info("입차 성공 개수: {}", successCount.get());
        Assertions.assertEquals(parkOperInfo.getCmprtCo(), successCount.get());
        log.info("입차 실패 개수: {}", failCount.get());
        Assertions.assertEquals(numOfUsers - parkOperInfo.getCmprtCo(), failCount.get());
    }

    private List<CarNumRequestDto> generateVehicleIds(int count) throws NoSuchFieldException, IllegalAccessException {
        List<CarNumRequestDto> vehicleIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CarNumRequestDto carNumRequestDto = new CarNumRequestDto();
            Field parkIdField = CarNumRequestDto.class.getDeclaredField("parkId");
            parkIdField.setAccessible(true);
            parkIdField.set(carNumRequestDto, 1L);

            Field carNumField = CarNumRequestDto.class.getDeclaredField("carNum");
            carNumField.setAccessible(true);
            carNumField.set(carNumRequestDto, "11가1" + String.format("%03d", i + 1));

            Field parkTimeField = CarNumRequestDto.class.getDeclaredField("parkingTime");
            parkTimeField.setAccessible(true);
            parkTimeField.set(carNumRequestDto, 1);
            vehicleIds.add(carNumRequestDto);
        }
        return vehicleIds;
    }
}
