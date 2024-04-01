package com.totainfo.eap.cp.trx.client.EAPRepCurModel;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
public class EAPRepCurModelO extends BaseTrxO {
    private String state; // 0 从机 1 主机

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}