package com.infrean_study.zone;

import com.infrean_study.domain.Zone;
import lombok.Data;

@Data
public class ZoneForm {

    private String zoneTitle;

    public String getCity(){
        return zoneTitle.substring(0, zoneTitle.indexOf("("));
    }

    public String getLocalNameOfCity(){
        return zoneTitle.substring(zoneTitle.indexOf("(") + 1, zoneTitle.indexOf(")"));
    }

    public String getProvince(){
        return zoneTitle.substring(zoneTitle.indexOf("/") + 1);
    }

    public Zone getZone() {
        return Zone.builder().city(this.getCity())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvince()).build();
    }

}
