package com.sparta.parknav.booking.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.parknav.booking.dto.ParkBookingInfoMgtDto;
import com.sparta.parknav.booking.entity.QParkBookingInfo;
import com.sparta.parknav.management.entity.QParkMgtInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ParkBookingInfoRepositoryImpl implements ParkBookingInfoRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QParkBookingInfo qParkBookingInfo = QParkBookingInfo.parkBookingInfo;
    private final QParkMgtInfo qParkMgtInfo = QParkMgtInfo.parkMgtInfo;

    @Override
    public Page<ParkBookingInfoMgtDto> findByMgtList(Long parkInfoId, int state, int sort, Pageable pageable) {
        BooleanBuilder whereBuilder = new BooleanBuilder();
        List<OrderSpecifier<?>> orderSpecifierList = new ArrayList<>();

        whereBuilder.and(qParkBookingInfo.parkInfo.id.eq(parkInfoId));

        switch (state) {
            case 1 -> whereBuilder.and(qParkMgtInfo.id.isNull().and(qParkBookingInfo.endTime.gt(LocalDateTime.now())));
            case 2 -> whereBuilder.and(qParkMgtInfo.id.isNotNull().and(qParkMgtInfo.exitTime.isNull()));
            default -> {
            }
        }

        switch (sort) {
            case 0 -> orderSpecifierList.add(qParkBookingInfo.startTime.desc().nullsLast());
            case 1 -> orderSpecifierList.add(qParkBookingInfo.endTime.desc().nullsLast());
            case 2 -> orderSpecifierList.add(qParkMgtInfo.enterTime.desc().nullsLast());
            case 3 -> orderSpecifierList.add(qParkMgtInfo.exitTime.desc().nullsLast());
            default -> {
            }
        }

        QueryResults<ParkBookingInfoMgtDto> queryReuslt= jpaQueryFactory.select(Projections.constructor(ParkBookingInfoMgtDto.class,
                        qParkBookingInfo.carNum, qParkBookingInfo.startTime, qParkBookingInfo.endTime,
                        qParkBookingInfo.user.id, qParkBookingInfo.exitTime, qParkMgtInfo.parkBookingInfo.id,
                        qParkMgtInfo.enterTime, qParkMgtInfo.exitTime, qParkMgtInfo.charge))
                .from(qParkBookingInfo)
                .leftJoin(qParkMgtInfo).on(qParkBookingInfo.id.eq(qParkMgtInfo.parkBookingInfo.id))
                .where(whereBuilder)
                .orderBy(orderSpecifierList.toArray(new OrderSpecifier[orderSpecifierList.size()]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        List<ParkBookingInfoMgtDto> parkBookingInfoMgtDtoList = queryReuslt.getResults();
        Long total = queryReuslt.getTotal();
        return new PageImpl<>(parkBookingInfoMgtDtoList,pageable,total);
    }
}
