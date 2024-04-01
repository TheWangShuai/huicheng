package com.totainfo.eap.cp.trx.gpib.GPIBWaferEndReport.GPIBWaferStartReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class GPIBWaferStartReportI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String equipmentNo;
    private String lotNo;
    private String waferId;
    private String pvWaferId;

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

    public String getPvWaferId() {
        return pvWaferId;
    }

    public void setPvWaferId(String pvWaferId) {
        this.pvWaferId = pvWaferId;
    }
}
