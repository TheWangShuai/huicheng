package com.totainfo.eap.cp.trx.kvm.KVMTimeReport;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

public class KVMTimeReportO extends BaseTrxO {
   private String eapId;

   private String opeContent;

   private String state;

    public String getEapId() {
        return eapId;
    }

    public void setEapId(String eapId) {
        this.eapId = eapId;
    }

    public String getOpeContent() {
        return opeContent;
    }

    public void setOpeContent(String opeContent) {
        this.opeContent = opeContent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
