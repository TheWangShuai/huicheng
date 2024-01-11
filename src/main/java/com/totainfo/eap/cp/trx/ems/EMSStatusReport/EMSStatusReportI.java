package com.totainfo.eap.cp.trx.ems.EMSStatusReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EMSStatusReportI extends BaseTrxI {
    private String eqpCommStatus;

    private String lastState;

    private String equipmentNo;

    private String remark;

    private String lastStateVal;

    public String getEqpCommStatus() {
        return eqpCommStatus;
    }

    public void setEqpCommStatus(String eqpCommStatus) {
        this.eqpCommStatus = eqpCommStatus;
    }

    public String getLastState() {
        return lastState;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastStateVal() {
        return lastStateVal;
    }

    public void setLastStateVal(String lastStateVal) {
        this.lastStateVal = lastStateVal;
    }
}
