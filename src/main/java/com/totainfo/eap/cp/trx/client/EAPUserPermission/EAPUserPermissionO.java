package com.totainfo.eap.cp.trx.client.EAPUserPermission;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author WangShuai
 * @date 2024/3/7
 */
public class EAPUserPermissionO extends BaseTrxO {

    private String actionFlg;

    public String getActionFlg() {
        return actionFlg;
    }

    public void setActionFlg(String actionFlg) {
        this.actionFlg = actionFlg;
    }

    public EAPUserPermissionO() {
    }

    public EAPUserPermissionO(String actionFlg) {
        this.actionFlg = actionFlg;
    }
}