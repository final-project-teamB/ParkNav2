package com.sparta.parknav.user.entity;

import com.sparta.parknav.parking.entity.ParkInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "ADMINS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String adminId;

    @Column(nullable = false)
    private String password;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "park_info_id")
    private ParkInfo parkInfo;

    @Builder
    public Admin(String adminId, String password, ParkInfo parkInfo) {
        this.adminId = adminId;
        this.password = password;
        this.parkInfo = parkInfo;
    }

    public static Admin of(String adminId, String password, ParkInfo parkInfo) {
        return builder()
                .adminId(adminId)
                .password(password)
                .parkInfo(parkInfo)
                .build();
    }
}
