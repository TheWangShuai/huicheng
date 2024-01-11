package com.totainfo.eap.cp.trx.ems.EMSWaferReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EMSWaferReportI extends BaseTrxI {
    private String lotNo;

    private String equipmentNo;

    private String waferNo;

    private String waferSeq;

    private String comment;

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

    public String getWaferNo() {
        return waferNo;
    }

    public void setWaferNo(String waferNo) {
        this.waferNo = waferNo;
    }

    public String getWaferSeq() {
        return waferSeq;
    }

    public void setWaferSeq(String waferSeq) {
        this.waferSeq = waferSeq;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
