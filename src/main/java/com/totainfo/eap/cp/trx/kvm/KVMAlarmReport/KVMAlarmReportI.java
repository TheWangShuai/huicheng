package com.totainfo.eap.cp.trx.kvm.KVMAlarmReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import sun.dc.pr.PRError;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:05
 */
public class KVMAlarmReportI extends BaseTrxI {
    private String alarmCode;
    private String alarmMessage;
    private String time;
    private String path;
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmMessage() {
        return alarmMessage;
    }

    public void setAlarmMessage(String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
