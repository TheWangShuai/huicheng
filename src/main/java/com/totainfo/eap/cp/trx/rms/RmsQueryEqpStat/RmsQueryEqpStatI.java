package com.totainfo.eap.cp.trx.rms.RmsQueryEqpStat;


import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2022年11月24日 9:27
 */
public class RmsQueryEqpStatI extends BaseTrxI {
    private String evtUsr;
    private String fabIdFk;
    private String toolId;


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
}
