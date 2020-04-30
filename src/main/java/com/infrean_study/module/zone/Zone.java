package com.infrean_study.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String city; //도시 이름
    @Column(nullable = false)
    private String localNameOfCity; // 지역 이름
    @Column(nullable = true)
    private String province; // 시-도

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}
