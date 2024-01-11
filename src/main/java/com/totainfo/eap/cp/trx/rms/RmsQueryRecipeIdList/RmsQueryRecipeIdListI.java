package com.totainfo.eap.cp.trx.rms.RmsQueryRecipeIdList;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2022年11月17日 13:02
 */
public class RmsQueryRecipeIdListI extends BaseTrxI {
    private String evtUsr;
    private String fabIdFk;
    private String toolId;
    private String toolType;
    private String data;
    private String success;
    private String errorMsg;

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }

    public String getFabIdFk() {
        return fabIdFk;
    }

    public void setFabIdFk(String fabIdFk) {
        this.fabIdFk = fabIdFk;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
