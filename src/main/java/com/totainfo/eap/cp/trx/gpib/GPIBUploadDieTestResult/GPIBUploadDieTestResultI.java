package com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

public class GPIBUploadDieTestResultI extends BaseTrxI {
    private String evtUsr;

    private String lotNo;

    private String waferId;

    private String remark;

    private String conputerName;

    private String equipmentNo;

    private String startCoorDinates;

    private String result;

    private List datas;

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getConputerName() {
        return conputerName;
    }

    public void setConputerName(String conputerName) {
        this.conputerName = conputerName;
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

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public List getDatas() {
        return datas;
    }

    public void setDatas(List datas) {
        this.datas = datas;
    }

    public String getStartCoorDinates() {
        return startCoorDinates;
    }

    public void setStartCoorDinates(String startCoorDinates) {
        this.startCoorDinates = startCoorDinates;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
