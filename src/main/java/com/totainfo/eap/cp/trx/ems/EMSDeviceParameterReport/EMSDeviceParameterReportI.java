package com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

public class EMSDeviceParameterReportI extends BaseTrxI {
    private String lotNo;

    private String equipmentNo;

    private String eqpType;

    private List<EMSDeviceParameterReportIA> paramList;

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

    public String getEqpType() {
        return eqpType;
    }

    public void setEqpType(String eqpType) {
        this.eqpType = eqpType;
    }

    public List<EMSDeviceParameterReportIA> getParamList() {
        return paramList;
    }

    public void setParamList(List<EMSDeviceParameterReportIA> paramList) {
        this.paramList = paramList;
    }

}
