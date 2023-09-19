package com.totainfo.eap.cp.trx.rms.RmsOnlineValidation;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2022年11月18日 11:41
 */
public class RmsOnlineValidationO extends BaseTrxO {
   private String jobId;
   private String result;
   private String reason;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
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
