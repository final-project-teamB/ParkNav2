package com.sparta.parknav.management.service;

import com.sparta.parknav.parking.entity.ParkOperInfo;
import lombok.Builder;

public class ParkingFeeCalculator {
    private int basicCharge;
    private int basicTime;
    private int additionalCharge;
    private int additionalUnitTime;

    @Builder
    private ParkingFeeCalculator(int basicCharge, int basicTime, int additionalCharge, int additionalUnitTime) {
        this.basicCharge = basicCharge;
        this.basicTime = basicTime;
        this.additionalCharge = additionalCharge;
        this.additionalUnitTime = additionalUnitTime;
    }

    public static ParkingFeeCalculator of(int chargeBsTime,int chargeBsChrg, int chargeAditUnitTime, int chargeAditUnitChrg) {
        return builder()
                .basicCharge(chargeBsTime)
                .basicTime(chargeBsChrg)
                .additionalUnitTime(chargeAditUnitTime)
                .additionalCharge(chargeAditUnitChrg)
                .build();
    }

    public static ParkingFeeCalculator from(ParkOperInfo parkOperInfo) {
        return builder()
                .basicCharge(parkOperInfo.getChargeBsChrg())
                .basicTime(parkOperInfo.getChargeBsTime())
                .additionalCharge(parkOperInfo.getChargeAditUnitChrg())
                .additionalUnitTime(parkOperInfo.getChargeAditUnitTime())
                .build();
    }

    public int calculateParkingFee(long parkingTime) {
        int parkingFee = basicCharge;

        if (parkingTime > basicTime) {
            long additionalTime = parkingTime - basicTime;
            int additionalFee = (int) Math.ceil((double) additionalTime / additionalUnitTime) * additionalCharge;
            parkingFee += additionalFee;
        }

        return parkingFee;
    }
}

