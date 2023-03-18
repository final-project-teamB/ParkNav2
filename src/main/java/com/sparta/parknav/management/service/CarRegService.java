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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CarRegService {

    private final CarRepository carRepository;

    @Transactional
    public ApiResponseDto<Void> regist(User user, CarRegist carRegist) {

        //등록된 차량인지 확인
        Car dupCar = carRepository.findByUserAndCarNum(user, carRegist.getCarNum());
        if (dupCar !=null) {
            throw new CustomException(ErrorType.ALREADY_REG_CAR);
        }
        //대표 차량이 있는지 확인
        Car repCar = carRepository.findByUserAndIsUsingIs(user, true);
        Boolean rep = carRegist.getIsUsing();
        if (repCar != null) {
            if (carRegist.getIsUsing()) {
                repCar.update(false);
            }
        } else {
            rep = true;
        }
        Car car = Car.of(carRegist.getCarNum(), user, rep);

        carRepository.save(car);
        return ResponseUtils.ok(MsgType.REGISTRATION_SUCCESSFULLY);
    }

    @Transactional
    public ApiResponseDto<Void> representative(User user, CarRegist carRegist) {

        Car car1 = carRepository.findByUserAndCarNum(user, carRegist.getCarNum());
        if (car1 == null) {
            throw new CustomException(ErrorType.NOT_FOUND_CAR);
        }

        Car car = carRepository.findByUserAndIsUsingIs(user, true);
        // 현 대표차량과 등록하려는 차량 번호가 같으면 예외
        if (Objects.equals(carRegist.getCarNum(), car.getCarNum())) {
            throw new CustomException(ErrorType.ALREADY_REG_REP_CAR);
        }
        car.update(false);
        car1.update(true);
        return ResponseUtils.ok(MsgType.REP_REG_SUCCESSFULLY);
    }
}
