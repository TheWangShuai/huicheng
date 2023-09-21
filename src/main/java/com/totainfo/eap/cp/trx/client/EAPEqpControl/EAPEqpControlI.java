package com.totainfo.eap.cp.trx.client.EAPEqpControl;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:34
 */
public class EAPEqpControlI extends BaseTrxI {
    private String userId;
    private String model;
    private boolean isCheckIn;

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

    public boolean getIsCheckIn() {
        return isCheckIn;
    }

    public void setIsCheckIn(boolean isCheckIn) {
        this.isCheckIn = isCheckIn;
    }
}
