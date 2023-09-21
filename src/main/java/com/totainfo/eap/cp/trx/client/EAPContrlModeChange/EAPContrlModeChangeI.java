package com.totainfo.eap.cp.trx.client.EAPContrlModeChange;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 9:55
 */
public class EAPContrlModeChangeI extends BaseTrxI {
    private String userId;
    private String model;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
