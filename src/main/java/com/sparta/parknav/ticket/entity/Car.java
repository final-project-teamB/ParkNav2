package com.sparta.parknav.ticket.entity;

import com.sparta.parknav.ticket.dto.request.CarNumRequestDto;
import com.sparta.parknav.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 20)
    private String carNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private User user;

    @Builder
    private Car(String carNum, User user) {
        this.carNum = carNum;
        this.user = user;
    }

    public static Car of(CarNumRequestDto requestDto, User user) {
        return builder()
                .carNum(requestDto.getCarNum())
                .user(user)
                .build();
    }
}
