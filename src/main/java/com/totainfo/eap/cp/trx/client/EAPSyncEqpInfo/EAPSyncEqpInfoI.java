package com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:56
 */
public class EAPSyncEqpInfoI extends BaseTrxI {
    private String userId;
    private String state;
    private String model;
    private String lotNo;
    private String probeCardId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getProberCardId() {
        return probeCardId;
    }

    public void setProberCardId(String proberCardId) {
        this.probeCardId = probeCardId;
    }
}
