package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkSearchResponseDto {
    private Long id;
    private int chargeAditUnitChrg;
    private int chargeAditUnitTime;
    private int chargeBsChrg;
    private int chargeBsTime;
    private int cmprtCo;
    private String parkCtgy;
    private String satClose;
    private String satOpen;
    private String sunClose;
    private String sunOpen;
    private String weekdayClose;
    private String weekdayOpen;
    private String address1;
    private String address2;
    private String la;
    private String lo;
    private String name;
    private int totCharge;

    @Builder
    private ParkSearchResponseDto(ParkOperInfo parkOperInfo, ParkInfo parkInfo, int totCharge) {
        this.id = parkOperInfo.getId();
        this.chargeAditUnitChrg =  parkOperInfo.getChargeAditUnitChrg();
        this.chargeAditUnitTime = parkOperInfo.getChargeAditUnitTime();
        this.chargeBsChrg = parkOperInfo.getChargeBsChrg();
        this.chargeBsTime = parkOperInfo.getChargeBsTime();
        this.cmprtCo = parkOperInfo.getCmprtCo();
        this.parkCtgy = parkOperInfo.getParkCtgy();
        this.satClose = parkOperInfo.getSatClose();
        this.satOpen = parkOperInfo.getSatOpen();
        this.sunClose = parkOperInfo.getSunClose();
        this.sunOpen = parkOperInfo.getSunOpen();
        this.weekdayClose = parkOperInfo.getWeekdayClose();
        this.weekdayOpen = parkOperInfo.getWeekdayOpen();
        this.address1 = parkInfo.getAddress1();
        this.address2 = parkInfo.getAddress2();
        this.la = parkInfo.getLa();
        this.lo = parkInfo.getLo();
        this.name = parkInfo.getName();
        this.totCharge = totCharge;
    }

    public static ParkSearchResponseDto of(ParkOperInfo parkOperInfo, ParkInfo parkInfo, int totCharge){
        return ParkSearchResponseDto.builder()
                .parkOperInfo(parkOperInfo)
                .parkInfo(parkInfo)
                .totCharge(totCharge)
                .build();
    }
}
