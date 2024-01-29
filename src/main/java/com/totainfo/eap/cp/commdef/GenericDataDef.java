package com.totainfo.eap.cp.commdef;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenericDataDef {

    public static String equipmentNo;

    public static String proberUrl;

    public static String testerUrl;

    public static String recipeBodyUrl;

    public static String eqpType;

    public static String rcmUrl;

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

    @Value("${client.recipeBodyUrl}")
    public void setRecipeBodyUrl(String recipeBodyUrl)
    {
        GenericDataDef.recipeBodyUrl = recipeBodyUrl;
    }
    @Value("${equipment.type}")
    public void setEqpType(String eqpType) {
        GenericDataDef.eqpType = eqpType;
    }
    @Value("${rcm.rcmUrl}")
    public void setRcmUrl(String rcmUrl) {GenericDataDef.rcmUrl = rcmUrl;}
}
