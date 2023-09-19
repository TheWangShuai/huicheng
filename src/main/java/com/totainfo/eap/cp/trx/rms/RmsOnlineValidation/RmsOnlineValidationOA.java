package com.totainfo.eap.cp.trx.rms.RmsOnlineValidation;

/**
 * @author xiaobin.Guo
 * @date 2023年01月04日 14:56
 */
public class RmsOnlineValidationOA {
    private String toolId;
    private String lotId;
    private String result;
    private String reason;

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
