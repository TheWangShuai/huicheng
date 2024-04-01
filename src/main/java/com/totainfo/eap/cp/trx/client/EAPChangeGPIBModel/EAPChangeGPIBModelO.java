package com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
public class EAPChangeGPIBModelO extends BaseTrxO {
    private String trxId;

    @Override
    public String getTrxId() {
        return trxId;
    }

    @Override
    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String getActionFlg() {
        return actionFlg;
    }

    public void setActionFlg(String actionFlg) {
        this.actionFlg = actionFlg;
    }

    private String actionFlg;

}