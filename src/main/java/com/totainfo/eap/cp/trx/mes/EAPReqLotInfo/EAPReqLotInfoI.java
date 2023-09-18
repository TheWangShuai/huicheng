package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:13
 */
public class EAPReqLotInfoI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String lotNo;
    private String proberId;
    private String equipmentNo;

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

    public String getProberId() {
        return proberId;
    }

    public void setProberId(String proberId) {
        this.proberId = proberId;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }
}
