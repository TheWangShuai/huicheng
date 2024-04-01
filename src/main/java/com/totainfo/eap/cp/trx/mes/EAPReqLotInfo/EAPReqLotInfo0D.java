package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxO;
import sun.dc.pr.PRError;


/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:14
 */
public class EAPReqLotInfo0D{
    private String lotInfo;
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

    public String getLotInfo() {
        return lotInfo;
    }

    public void setLotInfo(String lotInfo) {
        this.lotInfo = lotInfo;
    }
}
