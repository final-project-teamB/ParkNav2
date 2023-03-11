package com.sparta.parknav.users.entity;

import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String usersId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean isAdmin;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "park_info_id")
    private ParkInfo parkInfo;
}
