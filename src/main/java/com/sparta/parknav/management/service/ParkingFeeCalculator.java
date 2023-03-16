package com.sparta.parknav.management.service;

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

    public static ParkingFeeCalculator of(int charge_bs_time,int charge_bs_chrg, int charge_adit_unit_time, int charge_adit_unit_chrg) {
        return builder()
                .basicCharge(charge_bs_chrg)
                .basicTime(charge_bs_time)
                .additionalUnitTime(charge_adit_unit_time)
                .additionalCharge(charge_adit_unit_chrg)
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

