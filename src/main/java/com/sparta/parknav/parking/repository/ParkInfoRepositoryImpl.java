package com.sparta.parknav.parking.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.entity.QParkInfo;
import com.sparta.parknav.parking.entity.QParkOperInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParkInfoRepositoryImpl implements ParkInfoRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QParkOperInfo qParkOperInfo = QParkOperInfo.parkOperInfo;
    private final QParkInfo qParkInfo = QParkInfo.parkInfo;

    @Override
    public List<ParkOperInfo> findParkInfoWithOperInfoAndTypeQueryDsl(String x, String y, double distance, String type) {
        Double longitude = Double.parseDouble(x);
        Double latitude = Double.parseDouble(y);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Expressions.booleanTemplate(
                "(6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({4})) * sin(radians({5})))) < {6}",
                latitude,
                qParkInfo.la,
                qParkInfo.lo,
                longitude, latitude,
                qParkInfo.la,
                distance
        ));

        if (!type.equals("전체")) {
            builder.and(qParkOperInfo.parkCtgy.eq(type));
        }

        return jpaQueryFactory.selectFrom(qParkOperInfo)
                .join(qParkOperInfo.parkInfo, qParkInfo)
                .fetchJoin()
                .where(builder)
                .orderBy(Expressions.booleanTemplate(
                        "(6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({4})) * sin(radians({5}))))",
                        latitude,
                        qParkInfo.la,
                        qParkInfo.lo,
                        longitude, latitude,
                        qParkInfo.la
                ).asc())
                .fetch();
    }

}
