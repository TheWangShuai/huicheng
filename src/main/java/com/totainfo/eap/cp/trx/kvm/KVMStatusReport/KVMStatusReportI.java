package com.totainfo.eap.cp.trx.kvm.KVMStatusReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:08
 */
public class KVMStatusReportI extends BaseTrxI {
    private String eqpId;
    private String state;

    public String getEqpId() {
        return eqpId;
    }

    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
