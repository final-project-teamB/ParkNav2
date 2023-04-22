package com.sparta.parknav.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Builder
    private User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public static User of(String userId, String password) {
        return User.builder()
                .userId(userId)
                .password(password)
                .build();
    }
}
