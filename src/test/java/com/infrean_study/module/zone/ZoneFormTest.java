package com.infrean_study.zone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ZoneFormTest {

    private ZoneForm initZoneForm(){
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneTitle("Boryeong(보령시)/South Chungcheong");
        return zoneForm;
    }

    @Test
    @DisplayName("Zone 폼 리턴값 - 성공")
    public void zoneFormInput_pass(){
        ZoneForm zoneForm = initZoneForm();
        assertEquals(zoneForm.getCity(), "Boryeong");
        assertEquals(zoneForm.getLocalNameOfCity(), "보령시");
        assertEquals(zoneForm.getProvince(), "South Chungcheong");
    }

    @Test
    @DisplayName("Zone 폼 리턴값 - 실패")
    public void zoneFormInput_fail(){
        ZoneForm zoneForm = initZoneForm();
        assertNotEquals(zoneForm.getCity(), "Boryeong(");
        assertNotEquals(zoneForm.getLocalNameOfCity(), "보령시/");
        assertNotEquals(zoneForm.getProvince(), "South Chungcheongd");
    }

}