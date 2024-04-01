package com.totainfo.eap.cp.trx.client.EAPMessageSend;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 9:15
 */
public class EAPMessageSendI extends BaseTrxI {
    private boolean ispopUp;
    private int messageType;
    private String message;
    private String rtnCode;
    private String rtnMesg;

    public String getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRtnMesg() {
        return rtnMesg;
    }

    public void setRtnMesg(String rtnMesg) {
        this.rtnMesg = rtnMesg;
    }

    public boolean isIspopUp() {
        return ispopUp;
    }

    public void setIspopUp(boolean ispopUp) {
        this.ispopUp = ispopUp;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
