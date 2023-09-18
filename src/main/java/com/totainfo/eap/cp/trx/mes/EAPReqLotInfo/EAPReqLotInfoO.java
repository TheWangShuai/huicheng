package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:14
 */
public class EAPReqLotInfoO extends BaseTrxO {
    private EAPReqLotInfoOA lotInfo;

    public EAPReqLotInfoOA getLotInfo() {
        return lotInfo;
    }

    public void setLotInfo(EAPReqLotInfoOA lotInfo) {
        this.lotInfo = lotInfo;
    }
}
