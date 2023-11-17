package com.totainfo.eap.cp.trx.mes.EAPSyncProberCard;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:48
 */
public class MESSyncProberCardI  extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String ProberCardId;
    private String equipmentNo;
    private String lotNo;

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

    public String getProberCardId() {
        return ProberCardId;
    }

    public void setProberCardId(String proberCardId) {
        ProberCardId = proberCardId;
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
}
