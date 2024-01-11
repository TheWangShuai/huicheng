package com.totainfo.eap.cp.trx.ems.EMSAlarmReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EMSAlarmReportI extends BaseTrxI {
    private String alarmCode;
    private String alarmMessage;
    private String time;

    private String equipmentNo;

    private String alarmTab;

    private String lotNo;

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmMessage() {
        return alarmMessage;
    }

    public void setAlarmMessage(String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getAlarmTab() {
        return alarmTab;
    }

    public void setAlarmTab(String alarmTab) {
        this.alarmTab = alarmTab;
    }
}
