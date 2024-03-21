package com.totainfo.eap.cp.trx.kvm.KVMOperateend;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 9:57
 */
public class KVMOperateEndI extends BaseTrxI {
    private String actionFlg;
    private String eqpId;
    private String state;
    private String opeType;
    private String responseKey;
    private String opeContent;

    private String alarmCode;
    private String alarmMessage;
    private String time;
    private String path;
    private String testProgram;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTestProgram() {
        return testProgram;
    }

    public void setTestProgram(String testProgram) {
        this.testProgram = testProgram;
    }

    public void setOpeContent(String opeContent) {
        this.opeContent = opeContent;
    }

    @Override
    public String getActionFlg() {
        return actionFlg;
    }

    @Override
    public void setActionFlg(String actionFlg) {
        this.actionFlg = actionFlg;
    }

    public String getEqpId() {
        return eqpId;
    }

    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOpeType() {
        return opeType;
    }

    public void setOpeType(String opeType) {
        this.opeType = opeType;
    }

    public String getResponseKey() {
        return responseKey;
    }

    public void setResponseKey(String responseKey) {
        this.responseKey = responseKey;
    }

    public String getOpeContent() {
        return opeContent;
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
