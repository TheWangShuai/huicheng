package com.totainfo.eap.cp.trx.kvm.KVMTimeReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class KVMTimeReportI extends BaseTrxI {
    private String eqpId;

    public String getEqpId() {
        return eqpId;
    }

    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }
}
