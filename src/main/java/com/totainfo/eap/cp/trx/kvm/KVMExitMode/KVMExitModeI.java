package com.totainfo.eap.cp.trx.kvm.KVMExitMode;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class KVMExitModeI extends BaseTrxI {
    private String eqpId;

    public String getEqpId() {
        return eqpId;
    }

    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }
}
