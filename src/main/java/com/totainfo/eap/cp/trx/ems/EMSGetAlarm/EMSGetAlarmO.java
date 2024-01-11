package com.totainfo.eap.cp.trx.ems.EMSGetAlarm;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

public class EMSGetAlarmO extends BaseTrxO {
    private EMSGetAlarmOA itemList;

    public EMSGetAlarmOA getItemList() {
        return itemList;
    }

    public void setItemList(EMSGetAlarmOA itemList) {
        this.itemList = itemList;
    }

}
