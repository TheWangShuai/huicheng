package com.totainfo.eap.cp.trx.rms.RmsOnlineValidation;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2022年11月18日 11:01
 */
public class RmsOnlineValidationI extends BaseTrxI {

    private String evtUsr;
    private String fabIdFk;
    private String jobId;
    private List<RmsOnlineValidationIA> bisRecipeVOList;

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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<RmsOnlineValidationIA> getBisRecipeVOList() {
        return bisRecipeVOList;
    }

    public void setBisRecipeVOList(List<RmsOnlineValidationIA> bisRecipeVOList) {
        this.bisRecipeVOList = bisRecipeVOList;
    }
}
