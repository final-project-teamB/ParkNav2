package com.sparta.parknav.management.service;

import com.sparta.parknav._global.response.ApiResponseDto;
import com.sparta.parknav._global.response.MsgType;
import com.sparta.parknav._global.response.ResponseUtils;
import com.sparta.parknav.booking.entity.Car;
import com.sparta.parknav.booking.repository.CarRepository;
import com.sparta.parknav.management.dto.request.CarRegist;
import com.sparta.parknav.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CarRegService {

    private final CarRepository carRepository;

    @Transactional
    public ApiResponseDto<Void> regist(User user, CarRegist carRegistration) {

        // 대표차량인 차량 find
        Car car = carRepository.findByUserAndCarNumAndIsUsingIs(user, carRegistration.getCarNum(), true);
        if (car == null) {
            car = Car.of(carRegistration.getCarNum(), user, true);
        } else {
            if (carRegistration.getIsUsing()) {
                car.update(false);
            }
            car = Car.of(carRegistration.getCarNum(), user, carRegistration.getIsUsing());
        }
        carRepository.save(car);
        return ResponseUtils.ok(MsgType.REGISTRATION_SUCCESSFULLY);
    }

    public ApiResponseDto<MsgType> representative(User user, CarRegist carRegist) {
        return null;
    }
}
