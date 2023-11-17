package com.totainfo.eap.cp.trx.kvm.KVMSyncConnectionMode;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class KVMSyncConnectionModeI extends BaseTrxI {
    private String eqpId;

    public String getEqpId() {
        return eqpId;
    }

    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }
}
