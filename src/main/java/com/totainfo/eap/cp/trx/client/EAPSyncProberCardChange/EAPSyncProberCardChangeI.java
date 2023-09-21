package com.totainfo.eap.cp.trx.client.EAPSyncProberCardChange;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:10
 */
public class EAPSyncProberCardChangeI extends BaseTrxI {
    private String userId;
    private String proberCardId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProberCardId() {
        return proberCardId;
    }

    public void setProberCardId(String proberCardId) {
        this.proberCardId = proberCardId;
    }
}
