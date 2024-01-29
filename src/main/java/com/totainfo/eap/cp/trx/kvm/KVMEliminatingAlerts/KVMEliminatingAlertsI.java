package com.totainfo.eap.cp.trx.kvm.KVMEliminatingAlerts;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class KVMEliminatingAlertsI extends BaseTrxI {
    private String eapId;

    public String getEapId() {
        return eapId;
    }

    public void setEapId(String eapId) {
        this.eapId = eapId;
    }
}
