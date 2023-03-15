package com.sparta.parknav.parking.entity;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String address1;

    @Column(length = 100)
    private String address2;

    @Column(nullable = false, length = 20)
    private String la;

    @Column(nullable = false, length = 20)
    private String lo;

    @OneToOne(mappedBy = "parkInfo", cascade = CascadeType.ALL)
    private ParkOperInfo parkOperInfo;

    @Builder
    private ParkInfo(String name, String address1, String address2, String la, String lo) {
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
        this.la = la;
        this.lo = lo;
    }

    public static ParkInfo of(String name, String address1, String address2, String la, String lo) {
        return ParkInfo.builder()
                .name(name)
                .address1(address1)
                .address2(address2)
                .la(la)
                .lo(lo)
                .build();
    }
}
