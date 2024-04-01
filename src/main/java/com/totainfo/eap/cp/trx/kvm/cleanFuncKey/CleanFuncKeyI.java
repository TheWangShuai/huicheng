package com.totainfo.eap.cp.trx.kvm.cleanFuncKey;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/23
 */
public class CleanFuncKeyI extends BaseTrxI {

    private String clearFlg;
    private String equipmentNo;

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentState() {
        return equipmentState;
    }

    public void setEquipmentState(String equipmentState) {
        this.equipmentState = equipmentState;
    }

    private String equipmentState;

    public String getClearFlg() {
        return clearFlg;
    }

    public void setClearFlg(String clearFlg) {
        this.clearFlg = clearFlg;
    }
}