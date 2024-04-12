package com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 10:58
 */
public class EAPOperationInstructionI extends BaseTrxI {
    private String opeType;
    private List<EAPReqLotInfoOB>  instructList;

    private String lotId;

    private String userId;

    private String isFirst;

    public String getOpeType() {
        return opeType;
    }

    public void setOpeType(String opeType) {
        this.opeType = opeType;
    }

    public List<EAPReqLotInfoOB> getInstructList() {
        return instructList;
    }

    public void setInstructList(List<EAPReqLotInfoOB> instructList) {
        this.instructList = instructList;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(String isFirst) {
        this.isFirst = isFirst;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
