package com.sparta.parknav.parking.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkOperInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_info_id")
    private ParkInfo parkInfo;

    @Column(nullable = false, length = 10)
    private String weekdayOpen;

    @Column(nullable = false, length = 10)
    private String weekdayClose;

    @Column(nullable = false, length = 10)
    private String satOpen;

    @Column(nullable = false, length = 10)
    private String satClose;

    @Column(nullable = false, length = 10)
    private String sunOpen;

    @Column(nullable = false, length = 10)
    private String sunClose;

    @Column(nullable = false)
    private int chargeBsTime;

    @Column(nullable = false)
    private int chargeBsChrg;

    @Column(nullable = false)
    private int chargeAditUnitTime;

    @Column(nullable = false)
    private int chargeAditUtCharge;

    @Column(nullable = false)
    private int cmprtCo;

    @Column(nullable = false)
    private String parkCtgy;

}
