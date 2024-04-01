package com.totainfo.eap.cp.trx.kvm.cleanFuncKey;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author WangShuai
 * @date 2024/3/23
 */
public class CleanFuncKeyO extends BaseTrxO {

    private String opeContent;
    private String state;
    private String clearFlg;
    private String actionFlg;
    private String trxId;
    private String opeType;

    public String getActionFlg() {
        return actionFlg;
    }

    public void setActionFlg(String actionFlg) {
        this.actionFlg = actionFlg;
    }

    @Override
    public String getTrxId() {
        return trxId;
    }

    @Override
    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String getOpeType() {
        return opeType;
    }

    public void setOpeType(String opeType) {
        this.opeType = opeType;
    }

    public String getOpeContent() {
        return opeContent;
    }

    public void setOpeContent(String opeContent) {
        this.opeContent = opeContent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getClearFlg() {
        return clearFlg;
    }

    public void setClearFlg(String clearFlg) {
        this.clearFlg = clearFlg;
    }
}