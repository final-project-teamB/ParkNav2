package com.sparta.parknav.booking.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.parknav.booking.entity.ParkBookingByHour;
import com.sparta.parknav.booking.entity.QParkBookingByHour;
import com.sparta.parknav.parking.entity.QParkInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ParkBookingByHourRepositoryImpl implements ParkBookingByHourRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QParkBookingByHour qParkBookingByHour = QParkBookingByHour.parkBookingByHour;
    private final QParkInfo qParkInfo = QParkInfo.parkInfo;

    @Override
    public List<ParkBookingByHour> findByParkInfoIdAndFromStartDateToEndDate(Long parkInfoId, LocalDateTime startDate, LocalDateTime endDate) {

        LocalDate startDay = startDate.toLocalDate();
        LocalDate endDay = endDate.toLocalDate();
        long days = Period.between(startDay, endDay).getDays();
        int startTime = startDate.getHour();
        int endTime = endDate.getHour();
        if (endDate.toLocalTime().getMinute() == 0 && endDate.toLocalTime().getSecond() == 0) {
            endTime = endDate.minusHours(1).getHour();
        }

        List<ParkBookingByHour> result = new ArrayList<>();
        if (days == 0) {
            result = jpaQueryFactory.selectFrom(qParkBookingByHour)
                    .innerJoin(qParkBookingByHour.parkInfo, qParkInfo)
                    .fetchJoin()
                    .where(qParkBookingByHour.parkInfo.id.eq(parkInfoId)
                            .and(qParkBookingByHour.date.eq(startDay))
                            .and(qParkBookingByHour.time.between(startTime, endTime)))
                    .fetch();
        } else {
            List<ParkBookingByHour> resultFirstDay = jpaQueryFactory.selectFrom(qParkBookingByHour)
                    .innerJoin(qParkBookingByHour.parkInfo, qParkInfo)
                    .fetchJoin()
                    .where(qParkBookingByHour.parkInfo.id.eq(parkInfoId)
                            .and(qParkBookingByHour.date.eq(startDay))
                            .and(qParkBookingByHour.time.between(startTime, 23)))
                    .fetch();

            List<ParkBookingByHour> resultLastDay = jpaQueryFactory.selectFrom(qParkBookingByHour)
                    .innerJoin(qParkBookingByHour.parkInfo, qParkInfo)
                    .fetchJoin()
                    .where(qParkBookingByHour.parkInfo.id.eq(parkInfoId)
                            .and(qParkBookingByHour.date.eq(endDay))
                            .and(qParkBookingByHour.time.between(0, endTime)))
                    .fetch();

            result.addAll(resultFirstDay);
            result.addAll(resultLastDay);

            if (days > 1) {
                List<ParkBookingByHour> resultMiddleDays = jpaQueryFactory.selectFrom(qParkBookingByHour)
                        .innerJoin(qParkBookingByHour.parkInfo, qParkInfo)
                        .fetchJoin()
                        .where(qParkBookingByHour.parkInfo.id.eq(parkInfoId)
                                .and(qParkBookingByHour.date.between(startDay.plusDays(1), endDay.minusDays(1))))
                        .fetch();
                result.addAll(resultMiddleDays);
            }
        }

        return result;
    }
}
