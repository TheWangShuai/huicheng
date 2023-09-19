package com.totainfo.eap.cp.trx.rms.EapReportEqpStat;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2022年11月24日 9:32
 */
public class EapReportEqpStatI extends BaseTrxI {

    private String evtUsr;
    private String fabIdFk;
    private String toolId;
    private String toolStat;
    private String evtDesc;

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

    public String getToolStat() {
        return toolStat;
    }

    public void setToolStat(String toolStat) {
        this.toolStat = toolStat;
    }

    public String getEvtDesc() {
        return evtDesc;
    }

    public void setEvtDesc(String evtDesc) {
        this.evtDesc = evtDesc;
    }
}
