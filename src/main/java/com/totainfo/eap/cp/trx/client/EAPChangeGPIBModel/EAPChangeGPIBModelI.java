package com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
public class EAPChangeGPIBModelI extends BaseTrxI {
    private String rtnCode;
    private String rtnMesg;

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