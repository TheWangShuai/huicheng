package com.totainfo.eap.cp.trx.ems.EMSGetAlarm;

public class EMSGetAlarmOA {
    private String alarmCode;

    private String alarmText;

    private String alarmType;

    private String needClear;

    private String needRefund;

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmText() {
        return alarmText;
    }

    public void setAlarmText(String alarmText) {
        this.alarmText = alarmText;
    }

    public String getNeedClear() {
        return needClear;
    }

    public void setNeedClear(String needClear) {
        this.needClear = needClear;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getNeedRefund() {
        return needRefund;
    }
    public void setNeedRefund(String needRefund) {
        this.needRefund = needRefund;
    }
}
