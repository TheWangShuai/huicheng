package com.totainfo.eap.cp.trx.client.EAPMessageSendK;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/7
 */
public class EAPMessageSentKO extends BaseTrxI {

    private String rtnMessage;
    private String rtnCode;
    private boolean ispopUp;
    private int messageType;

    public EAPMessageSentKO(String rtnMessage, String rtnCode, boolean ispopUp, int messageType) {
        this.rtnMessage = rtnMessage;
        this.rtnCode = rtnCode;
        this.ispopUp = ispopUp;
        this.messageType = messageType;
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

    public EAPMessageSentKO() {
    }

    public EAPMessageSentKO(String rtnMessage, String rtnCode) {
        this.rtnMessage = rtnMessage;
        this.rtnCode = rtnCode;
    }

    public String getRtnMessage() {
        return rtnMessage;
    }

    public void setRtnMessage(String rtnMessage) {
        this.rtnMessage = rtnMessage;
    }

    public String getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }
}