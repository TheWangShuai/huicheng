package com.totainfo.eap.cp.trx.client.EAPLotIdRead;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 14:34
 */
public class EAPLotIdReadI extends BaseTrxI {
    private String lotId;
    private String proberId;
    private String userId;

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getProberId() {
        return proberId;
    }

    public void setProberId(String proberId) {
        this.proberId = proberId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
