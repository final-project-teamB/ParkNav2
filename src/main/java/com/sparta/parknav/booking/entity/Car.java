package com.sparta.parknav.booking.entity;

import com.sparta.parknav.management.dto.request.CarNumRequestDto;
import com.sparta.parknav.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String carNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean isUsing;

    @Builder
    private Car(String carNum, User user, Boolean isUsing) {
        this.carNum = carNum;
        this.user = user;
        this.isUsing = isUsing;
    }

    public static Car of(CarNumRequestDto requestDto, User user, Boolean isUsing) {
        return builder()
                .carNum(requestDto.getCarNum())
                .user(user)
                .isUsing(isUsing)
                .build();
    }
}
