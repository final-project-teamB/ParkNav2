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
    private ParkSearchResponseDto(ParkOperInfoDto parkOperInfoDto,int totCharge) {
        this.id = parkOperInfoDto.getId();
        this.chargeAditUnitChrg =  parkOperInfoDto.getChargeAditUnitChrg();
        this.chargeAditUnitTime = parkOperInfoDto.getChargeAditUnitTime();
        this.chargeBsChrg = parkOperInfoDto.getChargeBsChrg();
        this.chargeBsTime = parkOperInfoDto.getChargeBsTime();
        this.cmprtCo = parkOperInfoDto.getCmprtCo();
        this.parkCtgy = parkOperInfoDto.getParkCtgy();
        this.satClose = parkOperInfoDto.getSatClose();
        this.satOpen = parkOperInfoDto.getSatOpen();
        this.sunClose = parkOperInfoDto.getSunClose();
        this.sunOpen = parkOperInfoDto.getSunOpen();
        this.weekdayClose = parkOperInfoDto.getWeekdayClose();
        this.weekdayOpen = parkOperInfoDto.getWeekdayOpen();
        this.address1 = parkOperInfoDto.getAddress1();
        this.address2 = parkOperInfoDto.getAddress2();
        this.la = parkOperInfoDto.getLa();
        this.lo = parkOperInfoDto.getLo();
        this.name = parkOperInfoDto.getName();
        this.totCharge = totCharge;
    }

    public static ParkSearchResponseDto of(ParkOperInfoDto parkOperInfoDto,int totCharge){
        return ParkSearchResponseDto.builder()
                .parkOperInfoDto(parkOperInfoDto)
                .totCharge(totCharge)
                .build();
    }



}
