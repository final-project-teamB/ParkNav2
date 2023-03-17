package com.sparta.parknav.management.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
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
    public ApiResponseDto<Void> regist(User user, CarRegist carRegist) {

        // 대표차량인 차량 find
        Car car = carRepository.findByUserAndCarNumAndIsUsingIs(user, carRegist.getCarNum(), true);
        if (car == null) {
            car = Car.of(carRegist.getCarNum(), user, true);
        } else {
            if (carRegist.getIsUsing()) {
                car.update(false);
            }
            car = Car.of(carRegist.getCarNum(), user, carRegist.getIsUsing());
        }
        carRepository.save(car);
        return ResponseUtils.ok(MsgType.REGISTRATION_SUCCESSFULLY);
    }

    @Transactional
    public ApiResponseDto<Void> representative(User user, CarRegist carRegist) {

        Car car = carRepository.findByUserAndIsUsingIs(user, true);
        if (car == null) {
            throw new CustomException(ErrorType.NOT_FOUND_CAR);
        }
        car.update(false);

        Car newCar = Car.of(carRegist.getCarNum(), user, true);
        carRepository.save(newCar);
        return ResponseUtils.ok(MsgType.REP_REG_SUCCESSFULLY);
    }
}
