package com.sparta.parknav.parking.dto;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkOperInfoDto {

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
    private String name;
    private int totCharge;
    private String available;

    @Builder
    private ParkOperInfoDto(ParkOperInfo parkOperInfo, int totCharge, String available) {
        this.chargeAditUnitChrg = parkOperInfo.getChargeAditUnitChrg();
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
        this.address1 = parkOperInfo.getParkInfo().getAddress1();
        this.address2 = parkOperInfo.getParkInfo().getAddress2();
        this.name = parkOperInfo.getParkInfo().getName();
        this.totCharge = totCharge;
        this.available = available;
    }

    public static ParkOperInfoDto of(ParkOperInfo parkOperInfo, int totCharge, String available) {
        return ParkOperInfoDto.builder()
                .parkOperInfo(parkOperInfo)
                .totCharge(totCharge)
                .available(available)
                .build();
    }
}
