package com.totainfo.eap.cp.trx.mes.EAPReqCheckIn;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:19
 */
public class EAPReqCheckInI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String lotNo;
    private String equipmentNo;
    private String ProberCard;
    private String Temperature;
    private String TestProgram;
    private String Device;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getProberCard() {
        return ProberCard;
    }

    public void setProberCard(String proberCard) {
        ProberCard = proberCard;
    }

    public String getTemperature() {
        return Temperature;
    }

    public void setTemperature(String temperature) {
        Temperature = temperature;
    }

    public String getTestProgram() {
        return TestProgram;
    }

    public void setTestProgram(String testProgram) {
        TestProgram = testProgram;
    }

    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }
}
