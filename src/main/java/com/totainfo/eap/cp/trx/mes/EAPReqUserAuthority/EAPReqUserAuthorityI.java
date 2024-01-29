package com.totainfo.eap.cp.trx.mes.EAPReqUserAuthority;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EAPReqUserAuthorityI extends BaseTrxI {
    private String computerName;

    private String evtUsr;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }
}
