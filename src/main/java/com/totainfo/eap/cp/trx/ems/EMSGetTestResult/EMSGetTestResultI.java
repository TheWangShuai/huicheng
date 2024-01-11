package com.totainfo.eap.cp.trx.ems.EMSGetTestResult;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EMSGetTestResultI extends BaseTrxI {
    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
