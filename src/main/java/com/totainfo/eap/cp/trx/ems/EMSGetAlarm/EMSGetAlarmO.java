package com.totainfo.eap.cp.trx.ems.EMSGetAlarm;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

import java.util.List;

public class EMSGetAlarmO extends BaseTrxO {
    private List<EMSGetAlarmOA> itemList;

    public List<EMSGetAlarmOA> getItemList() {
        return itemList;
    }

    public void setItemList(List<EMSGetAlarmOA> itemList) {
        this.itemList = itemList;
    }

}
