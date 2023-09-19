package com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 10:58
 */
public class EAPOperationInstructionI extends BaseTrxI {
    private String opeType;
    private List<EAPOperationInstructionIA>  instructList;


    public String getOpeType() {
        return opeType;
    }

    public void setOpeType(String opeType) {
        this.opeType = opeType;
    }

    public List<EAPOperationInstructionIA> getInstructList() {
        return instructList;
    }

    public void setInstructList(List<EAPOperationInstructionIA> instructList) {
        this.instructList = instructList;
    }
}
