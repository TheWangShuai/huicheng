package com.totainfo.eap.cp.trx.ems.EMSGetAlarm;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EMSGetAlarmI extends BaseTrxI {
    private String equipmentType;

    private String alarmCode;

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }
}
