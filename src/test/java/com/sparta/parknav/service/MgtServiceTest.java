package com.sparta.parknav.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.management.repository.ParkMgtInfoRepository;
import com.sparta.parknav.management.service.MgtService;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.user.entity.Admin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class MgtServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MgtServiceTest2.class);
    @Autowired
    private MgtService mgtService;

    @Transactional
    @Test
    public void testEnter() throws NoSuchFieldException, IllegalAccessException, InterruptedException {

        // Given
        log.info("enter 동시성 테스트 시작");

        Long parkId = 5L;
        int cmptrCo = 10;
        // admin 객체를 생성하기 위한 parkInfo 생성
        ParkInfo parkInfo;
        parkInfo = ParkInfo.of("군산오름 주차장", "제주특별자치도 서귀포시 안덕면 창천리 산 3-1", "", "33.2515973888661", "126.367481160108");
        Field parkInfoField = ParkInfo.class.getDeclaredField("id");
        parkInfoField.setAccessible(true);
        parkInfoField.set(parkInfo, parkId);
        log.info("parkInfo의 ID : {} ", (parkInfo.getId()));

        Admin admin;
        // mock 객체로 admin 생성
        admin = Mockito.mock(Admin.class, CALLS_REAL_METHODS);
        // Admin 엔티티의 parkInfo 필드를 선언
        Field ParkInfoField = Admin.class.getDeclaredField("parkInfo");
        // parkInfo 필드를 private - > public 으로 변경
        ParkInfoField.setAccessible(true);
        // parkInfo 객체를 admin 객체의 parkInfo 필드에 set
        ParkInfoField.set(admin, parkInfo);

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
        Assertions.assertEquals(cmptrCo, successCount.get());
        log.info("입차 성공 개수: {}", successCount.get());
        Assertions.assertEquals(numOfUsers - cmptrCo, failCount.get());
        log.info("입차 실패 개수: {}", failCount.get());
    }

    private List<CarNumRequestDto> generateVehicleIds(int count) throws NoSuchFieldException, IllegalAccessException {
        List<CarNumRequestDto> vehicleIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CarNumRequestDto carNumRequestDto = new CarNumRequestDto();
            Field parkIdField = CarNumRequestDto.class.getDeclaredField("parkId");
            parkIdField.setAccessible(true);
            parkIdField.set(carNumRequestDto, 5L);

            Field carNumField = CarNumRequestDto.class.getDeclaredField("carNum");
            carNumField.setAccessible(true);
            carNumField.set(carNumRequestDto, "11가1" + String.format("%03d", i + 1));
            vehicleIds.add(carNumRequestDto);
        }
        return vehicleIds;
    }
}

