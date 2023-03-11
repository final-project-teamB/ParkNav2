package com.sparta.parknav.ticket.entity;

import com.sparta.parknav.users.entity.Users;
import lombok.AccessLevel;
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

    @Column(nullable = false,length = 20)
    private String car_num;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private Users users;
}
