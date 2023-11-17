package com.totainfo.eap.cp.trx.kvm.KVMSyncConnectionMode;

import com.totainfo.eap.cp.base.trx.BaseTrxO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class KVMSyncConnectionModeO extends BaseTrxO {
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
