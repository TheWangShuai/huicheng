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
