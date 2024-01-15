package com.totainfo.eap.cp.trx.gpib.GPIBDieInfoReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2024年01月14日 15:46
 */
public class GPIBDieInfoReportI extends BaseTrxI {
    private String waferId;
    private String startCoorDinate;
    private String siteNum;
    private String result;
    private String notchDirection;

    public String getWaferId() {
        return waferId;
    }

    public void setWaferId(String waferId) {
        this.waferId = waferId;
    }

    public String getStartCoorDinate() {
        return startCoorDinate;
    }

    public void setStartCoorDinate(String startCoorDinate) {
        this.startCoorDinate = startCoorDinate;
    }

    public String getSiteNum() {
        return siteNum;
    }

    public void setSiteNum(String siteNum) {
        this.siteNum = siteNum;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNotchDirection() {
        return notchDirection;
    }

    public void setNotchDirection(String notchDirection) {
        this.notchDirection = notchDirection;
    }
}
