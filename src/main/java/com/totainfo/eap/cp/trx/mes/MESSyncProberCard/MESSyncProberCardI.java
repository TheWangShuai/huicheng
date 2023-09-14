package com.totainfo.eap.cp.trx.mes.MESSyncProberCard;

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
}
