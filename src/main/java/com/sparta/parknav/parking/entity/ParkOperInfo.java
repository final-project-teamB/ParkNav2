package com.sparta.parknav.parking.entity;

import lombok.AccessLevel;
import lombok.Builder;
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
    private int chargeAditUnitChrg;

    @Column(nullable = false)
    private int cmprtCo;

    @Column(nullable = false)
    private String parkCtgy;

    @Builder
    private ParkOperInfo(ParkInfo parkInfo, String weekdayOpen, String weekdayClose, String satOpen, String satClose, String sunOpen, String sunClose, int chargeBsTime, int chargeBsChrg, int chargeAditUnitTime, int chargeAditUnitChrg, int cmprtCo, String parkCtgy) {
        this.parkInfo = parkInfo;
        this.weekdayOpen = weekdayOpen;
        this.weekdayClose = weekdayClose;
        this.satOpen = satOpen;
        this.satClose = satClose;
        this.sunOpen = sunOpen;
        this.sunClose = sunClose;
        this.chargeBsTime = chargeBsTime;
        this.chargeBsChrg = chargeBsChrg;
        this.chargeAditUnitTime = chargeAditUnitTime;
        this.chargeAditUnitChrg = chargeAditUnitChrg;
        this.cmprtCo = cmprtCo;
        this.parkCtgy = parkCtgy;
    }

    public static ParkOperInfo of(ParkInfo parkInfo, String parkCtgy) {
        return ParkOperInfo.builder()
                .parkInfo(parkInfo)
                .parkCtgy(parkCtgy)
                .build();
    }

    public void update(String weekdayOpen, String weekdayClose, String satOpen, String satClose, String sunOpen, String sunClose, int chargeBsTime, int chargeBsChrg, int chargeAditUnitTime, int chargeAditUnitChrg, int cmprtCo) {
        this.weekdayOpen = weekdayOpen;
        this.weekdayClose = weekdayClose;
        this.satOpen = satOpen;
        this.satClose = satClose;
        this.sunOpen = sunOpen;
        this.sunClose = sunClose;
        this.chargeBsTime = chargeBsTime;
        this.chargeBsChrg = chargeBsChrg;
        this.chargeAditUnitTime = chargeAditUnitTime;
        this.chargeAditUnitChrg = chargeAditUnitChrg;
        this.cmprtCo = cmprtCo;
    }
}
