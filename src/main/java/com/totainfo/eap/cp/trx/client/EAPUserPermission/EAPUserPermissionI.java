package com.totainfo.eap.cp.trx.client.EAPUserPermission;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/7
 */
public class EAPUserPermissionI extends BaseTrxI {

    private String userId;
    private String passWord;

    public EAPUserPermissionI() {
    }

    public EAPUserPermissionI(String userId, String passWord) {
        this.userId = userId;
        this.passWord = passWord;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}