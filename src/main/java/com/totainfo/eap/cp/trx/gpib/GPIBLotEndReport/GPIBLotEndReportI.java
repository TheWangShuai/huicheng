package com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class GPIBLotEndReportI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
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
