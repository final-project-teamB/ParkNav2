package com.sparta.parknav.parking.repository;

import com.sparta.parknav._global.config.QuerydslConfiguration;
import com.sparta.parknav.parking.dto.ParkSearchResponseDto;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.parking.entity.ParkOperInfo;
import com.sparta.parknav.parking.entity.ParkType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(QuerydslConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParkInfoRepositoryTest {
    @Autowired
    private ParkInfoRepository parkInfoRepository;

    @Test
    void findNameTest(){
        // given
        ParkInfo parkInfo = ParkInfo.of("테스트주차장", "주소1", "주소2", "31","123");
        ParkInfo parkInfo2 = ParkInfo.of("테스트주차장2", "주소1", "주소2", "31","123");
        ParkInfo parkInfo3 = ParkInfo.of("테스트주차장3", "주소1", "주소2", "31","123");
        List<ParkInfo> parkInfoList = new ArrayList<>();
        parkInfoList.add(parkInfo);
        parkInfoList.add(parkInfo2);
        parkInfoList.add(parkInfo3);

        parkInfoRepository.saveAll(parkInfoList);
        parkInfoRepository.flush();

        // when
        List<ParkInfo> found = parkInfoRepository.findByNameContains(parkInfo.getName());

        // then
        assertThat(found.get(0).getName()).isEqualTo(parkInfo.getName());
        assertThat(found.get(0).getAddress1()).isEqualTo(parkInfo.getAddress1());
        assertThat(found.get(0).getAddress2()).isEqualTo(parkInfo.getAddress2());
        assertThat(found.get(0).getLa()).isEqualTo(parkInfo.getLa());
        assertThat(found.get(0).getLo()).isEqualTo(parkInfo.getLo());
    }

    @Test
    void findAllByIdBetween(){
        // given
        ParkInfo parkInfo = ParkInfo.of("테스트주차장", "주소1", "주소2", "31","123");
        ReflectionTestUtils.setField(parkInfo, "id", 1L);
        ParkInfo parkInfo2 = ParkInfo.of("테스트주차장2", "주소1", "주소2", "31","123");
        ReflectionTestUtils.setField(parkInfo2, "id", 2L);
        ParkInfo parkInfo3 = ParkInfo.of("테스트주차장3", "주소1", "주소2", "31","123");
        ReflectionTestUtils.setField(parkInfo3, "id", 3L);
        List<ParkInfo> parkInfoList = new ArrayList<>();
        parkInfoList.add(parkInfo);
        parkInfoList.add(parkInfo2);
        parkInfoList.add(parkInfo3);
        parkInfoRepository.saveAll(parkInfoList);
        parkInfoRepository.flush();

        // when
        List<ParkInfo> found = parkInfoRepository.findAllByIdBetween(1L,2L);

        // then
        assertThat(found.size()).isEqualTo(2);

    }
}