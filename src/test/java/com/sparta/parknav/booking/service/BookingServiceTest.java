package com.sparta.parknav.booking.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav.booking.dto.BookingInfoRequestDto;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.repository.ParkInfoRepository;
import com.sparta.parknav.parking.repository.ParkOperInfoRepository;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(properties = "spring.profiles.active:test")
class BookingServiceTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ParkInfoRepository parkInfoRepository;
    @Autowired
    private ParkOperInfoRepository parkOperInfoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarRepository carRepository;

    @Test
    void bookingPark() throws InterruptedException {
        //when
        ParkInfo parkInfo = parkInfoRepository.save(ParkInfo.of("테스트주차장", "테스트주소1", "테스트주소2", "33.2501489768202", "126.563230508718"));
        ParkOperInfo parkOperInfoTmp = ParkOperInfo.of(parkInfo, "민영");
        parkOperInfoTmp.update("00:00", "23:59","00:00", "23:59","00:00", "23:59",30, 1000, 30, 500, 10);
        parkOperInfoRepository.save(parkOperInfoTmp);
        int numOfUsers = 20;
        // 대기하는 스레드의 숫자를 지정
        CountDownLatch endLatch = new CountDownLatch(numOfUsers);
        ExecutorService executorService = Executors.newFixedThreadPool(numOfUsers);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ParkOperInfo parkOperInfo = parkOperInfoRepository.findByParkInfoId(1L).get();
        createUser(numOfUsers);
        BookingInfoRequestDto bookingInfoRequestDto = BookingInfoRequestDto.of(LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        List<User> userList = new ArrayList<>();
        for (long i = 1; i <= numOfUsers; i++) {
            userList.add(userRepository.findById(i).get());
        }

        //given
        for (User user: userList){
            executorService.execute(() -> {
                try {
                    bookingService.bookingPark(parkInfo.getId(), bookingInfoRequestDto, user);
                    successCount.getAndIncrement();
                    log.info("예약 성공 | 예약유저 : {}", user.getUserId());
                } catch (CustomException e) {
                    failCount.getAndIncrement();
                    log.info(e.getMessage());
                }
                //스레드가 끝나면 -1 을 해서 endLatch를 1 줄임.
                endLatch.countDown();
            });
        }

        // Then
        log.info("enter 동시성 테스트 결과 검증");
        // 모든 스레드들이 끝날때까지 대기. 모든 스레드들이 끝나면 다음 코드 실행, 즉 endLatch가 0이 되면 다음 코드 실행
        endLatch.await();
        log.info("예약 성공 개수: {}", successCount.get());
        Assertions.assertEquals(parkOperInfo.getCmprtCo(), successCount.get());
        log.info("예약 실패 개수: {}", failCount.get());
        Assertions.assertEquals(numOfUsers - parkOperInfo.getCmprtCo(), failCount.get());
    }

    private void createUser(int count){
        List<User> userList = new ArrayList<>();
        List<Car> carList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = User.of("user"+i,"1234");
            userList.add(user);
            carList.add(Car.of("11가1"+String.format("%03d", i + 1),user,true ));
        }
        userRepository.saveAll(userList);
        carRepository.saveAll(carList);
    }

}