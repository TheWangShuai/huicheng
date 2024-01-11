package com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:47
 */
public class EAPUploadMarkResultO extends BaseTrxO {
    private String paramName;
    private String paramValue;

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}
