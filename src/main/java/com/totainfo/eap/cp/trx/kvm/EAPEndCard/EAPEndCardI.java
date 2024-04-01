package com.totainfo.eap.cp.trx.kvm.EAPEndCard;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

public class EAPEndCardI extends BaseTrxI {

    private String dieX;
    private String dieY;

    public String getDieX() {
        return dieX;
    }

    public void setDieX(String dieX) {
        this.dieX = dieX;
    }

    public String getDieY() {
        return dieY;
    }

    public void setDieY(String dieY) {
        this.dieY = dieY;
    }
}
