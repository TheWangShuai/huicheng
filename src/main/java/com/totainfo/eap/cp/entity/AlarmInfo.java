package com.totainfo.eap.cp.entity;

public class AlarmInfo {
    private String alarmCode;
    private String time;

    private String id;

    private String alarmText;
    private String alarmImg;

    public String getTime() {
        return time;
    }

    public String getAlarmImg() {
        return alarmImg;
    }

    public void setAlarmImg(String alarmImg) {
        this.alarmImg = alarmImg;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlarmText() {
        return alarmText;
    }

    public void setAlarmText(String alarmText) {
        this.alarmText = alarmText;
    }

}
