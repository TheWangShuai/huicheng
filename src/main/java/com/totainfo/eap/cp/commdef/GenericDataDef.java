package com.totainfo.eap.cp.commdef;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenericDataDef {

    public static String equipmentNo;

    public static String proberUrl;

    public static String testerUrl;

    @Value("${equipment.id}")
    public void setEquipmentNo(String equipmentNo) {
        GenericDataDef.equipmentNo = equipmentNo;
    }

    @Value("${equipment.proberUrl}")
    public void setProberUrl(String proberUrl) {
        GenericDataDef.proberUrl = proberUrl;
    }

    @Value("${equipment.testerUrl}")
    public void setTesterUrl(String testerUrl) {
        GenericDataDef.testerUrl = testerUrl;
    }
}
