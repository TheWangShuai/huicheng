package com.totainfo.eap.cp.trx.ems.EMSGetTestResult;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

public class EMSGetTestResultO extends BaseTrxO {
    private EMSGetTestResultOA itemList;

    public EMSGetTestResultOA getItemList() {
        return itemList;
    }

    public void setItemList(EMSGetTestResultOA itemList) {
        this.itemList = itemList;
    }
}
