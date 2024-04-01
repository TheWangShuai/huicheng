package com.totainfo.eap.cp.trx.client.EAPRepCurModel;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
public class EAPRepCurModelI extends BaseTrxI {
    private String state; // 0 从机 1 主机
    private String rtnCode;
    private String rtnMesg;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRtnMesg() {
        return rtnMesg;
    }

    public void setRtnMesg(String rtnMesg) {
        this.rtnMesg = rtnMesg;
    }
}