package com.totainfo.eap.cp.trx.ems.EMSLotinfoReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.sql.Timestamp;

public class EMSLotInfoReportI extends BaseTrxI {
    private String lotNo;

    private String deviceName;

    private String equipmentNo;

    private String testProgram;

    private String proberCard;

    private String processState;

    private String operator;

    private String temperature;

    private String comment;

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getTestProgram() {
        return testProgram;
    }

    public void setTestProgram(String testProgram) {
        this.testProgram = testProgram;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProberCard() {
        return proberCard;
    }

    public void setProberCard(String proberCard) {
        this.proberCard = proberCard;
    }

    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
