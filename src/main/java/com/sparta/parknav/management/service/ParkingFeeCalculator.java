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

    public static ParkingFeeCalculator from(ParkOperInfo parkOperInfo) {
        return builder()
                .basicCharge(parkOperInfo.getChargeBsChrg())
                .basicTime(parkOperInfo.getChargeBsTime())
                .additionalCharge(parkOperInfo.getChargeAditUnitChrg())
                .additionalUnitTime(parkOperInfo.getChargeAditUnitTime())
                .build();
    }

    public static int calculateParkingFee(long parkingTime, ParkOperInfo parkOperInfo) {

        ParkingFeeCalculator calculator = ParkingFeeCalculator.from(parkOperInfo);

        int parkingFee = calculator.basicCharge;

        long additionalTime = Math.max(0, parkingTime - calculator.basicTime);
        int additionalFee = (int) Math.ceil((double) additionalTime / calculator.additionalUnitTime) * calculator.additionalCharge;
        parkingFee += additionalFee;

        return parkingFee;
    }

    public static int calculateParkingFee(long parkingTime, ParkOperInfo parkOperInfo, long overTime) {

        ParkingFeeCalculator calculator = ParkingFeeCalculator.from(parkOperInfo);

        int parkingFee = calculateParkingFee(parkingTime, parkOperInfo);
        int penaltyFee = (int) Math.ceil((double) overTime / calculator.additionalUnitTime) * calculator.additionalCharge;

        return parkingFee + penaltyFee;
    }

}

