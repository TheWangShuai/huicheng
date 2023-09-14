package com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:03
 */
public class EAPSingleParamCollectionO extends BaseTrxO {
    private String parameterValue;

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
}
