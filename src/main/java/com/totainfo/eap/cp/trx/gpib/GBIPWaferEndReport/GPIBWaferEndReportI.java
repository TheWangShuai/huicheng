package com.totainfo.eap.cp.trx.gpib.GBIPWaferEndReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/13
 */
public class GPIBWaferEndReportI extends BaseTrxI {

    private String computerName;
    private String evtUsr;
    private String equipmentNo;
    private String lotNo;
    private String waferId;

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

    public String getWaferId() {
        return waferId;
    }

    public void setWaferId(String waferId) {
        this.waferId = waferId;
    }

    public GPIBWaferEndReportI() {
    }

    public GPIBWaferEndReportI(String computerName, String evtUsr, String equipmentNo, String lotNo, String waferId) {
        this.computerName = computerName;
        this.evtUsr = evtUsr;
        this.equipmentNo = equipmentNo;
        this.lotNo = lotNo;
        this.waferId = waferId;
    }
}