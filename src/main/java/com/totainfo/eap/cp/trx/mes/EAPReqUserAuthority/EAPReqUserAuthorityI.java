package com.totainfo.eap.cp.trx.mes.EAPReqUserAuthority;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EAPReqUserAuthorityI extends BaseTrxI {
    private String computerName;

    private String userId;

    private String evtUsr;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }
}
