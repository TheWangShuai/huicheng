package com.totainfo.eap.cp.trx.rcm;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EapReportAlarmInfoI extends BaseTrxI {
    private String equipmentNo;

    private String equipmentState;

    private String lotId;

    private String alarmCode;

    private String alarmMessage;

    private String alarmBeginTime;

    private String alarmEndTime;

    private String waferDates;

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentState() {
        return equipmentState;
    }

    public void setEquipmentState(String equipmentState) {
        this.equipmentState = equipmentState;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

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

    public String getAlarmBeginTime() {
        return alarmBeginTime;
    }

    public void setAlarmBeginTime(String alarmBeginTime) {
        this.alarmBeginTime = alarmBeginTime;
    }

    public String getAlarmEndTime() {
        return alarmEndTime;
    }

    public void setAlarmEndTime(String alarmEndTime) {
        this.alarmEndTime = alarmEndTime;
    }

    public String getWaferDates() {
        return waferDates;
    }

    public void setWaferDates(String waferDates) {
        this.waferDates = waferDates;
    }
}
