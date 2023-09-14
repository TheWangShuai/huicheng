package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:14
 */
public class EAPReqLotInfoO extends BaseTrxO {
    private EAPReqLotInfoOA rtnData;

    public EAPReqLotInfoOA getRtnData() {
        return rtnData;
    }

    public void setRtnData(EAPReqLotInfoOA rtnData) {
        this.rtnData = rtnData;
    }
}
