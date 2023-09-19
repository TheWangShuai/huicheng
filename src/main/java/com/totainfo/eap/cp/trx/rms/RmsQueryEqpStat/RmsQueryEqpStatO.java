package com.totainfo.eap.cp.trx.rms.RmsQueryEqpStat;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2022年11月24日 9:28
 */
public class RmsQueryEqpStatO extends BaseTrxO {

    private String toolId;
    private String toolSat;

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getToolSat() {
        return toolSat;
    }

    public void setToolSat(String toolSat) {
        this.toolSat = toolSat;
    }
}
