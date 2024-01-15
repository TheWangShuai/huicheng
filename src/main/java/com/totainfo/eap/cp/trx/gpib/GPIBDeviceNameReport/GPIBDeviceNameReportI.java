package com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:56
 */
public class GPIBDeviceNameReportI extends BaseTrxI {
    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
