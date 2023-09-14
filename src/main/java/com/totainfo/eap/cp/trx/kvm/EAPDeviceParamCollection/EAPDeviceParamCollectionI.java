package com.totainfo.eap.cp.trx.kvm.EAPDeviceParamCollection;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:04
 */
public class EAPDeviceParamCollectionI extends BaseTrxI {
    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
