package com.totainfo.eap.cp.trx.client.EAPReportDieInfo;

/**
 * @author WangShuai
 * @date 2024/4/3
 */
public class DieInfoOA {
    private String workId;
    private int dieCount;
    private String deviceName;

    private String waferStartTime;
    private String waferEndTime;

    public String getWaferStartTime() {
        return waferStartTime;
    }

    public void setWaferStartTime(String waferStartTime) {
        this.waferStartTime = waferStartTime;
    }

    public String getWaferEndTime() {
        return waferEndTime;
    }

    public void setWaferEndTime(String waferEndTime) {
        this.waferEndTime = waferEndTime;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public int getDieCount() {
        return dieCount;
    }

    public void setDieCount(int dieCount) {
        this.dieCount = dieCount;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}