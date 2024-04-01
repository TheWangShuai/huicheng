package com.totainfo.eap.cp.trx.mes.EAPLotInfoBase;

import com.totainfo.eap.cp.base.trx.BaseTrxO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;

/**
 * @author WangShuai
 * @date 2024/3/17
 */
public class EAPLotInfoBaseO extends BaseTrxO {

    private EAPReqLotInfoOA lotInfo;

    public EAPReqLotInfoOA getLotInfo() {
        return lotInfo;
    }

    public void setLotInfo(EAPReqLotInfoOA lotInfo) {
        this.lotInfo = lotInfo;
    }

}