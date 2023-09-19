package com.totainfo.eap.cp.entity;

import java.util.Map;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 10:14
 */
public class LotInfo {
    private String lotId;
    private String device;
    private String proberCard;
    private String testProgram;
    private String loadBoardId;
    private String deviceId;
    private String userId;

    private Map<String, String> paramMap;

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getProberCard() {
        return proberCard;
    }

    public void setProberCard(String proberCard) {
        this.proberCard = proberCard;
    }

    public String getTestProgram() {
        return testProgram;
    }

    public void setTestProgram(String testProgram) {
        this.testProgram = testProgram;
    }

    public String getLoadBoardId() {
        return loadBoardId;
    }

    public void setLoadBoardId(String loadBoardId) {
        this.loadBoardId = loadBoardId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }
}
