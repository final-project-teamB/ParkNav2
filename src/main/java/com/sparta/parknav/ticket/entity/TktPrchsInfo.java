package com.sparta.parknav.ticket.entity;

import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.users.entity.Users;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TktPrchsInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime prchs_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private ParkInfo parkInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID", nullable = false)
    private Car car;

    @Builder
    private TktPrchsInfo(LocalDateTime prchs_date, Users users) {
        this.prchs_date = prchs_date;
        this.users = users;
    }
}
