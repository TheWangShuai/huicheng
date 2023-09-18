package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:14
 */
public class EAPReqLotInfoOA {
    private String waferLot;
    private String device;
    private String proberCard;
    private String testPropram;
    private String loadBoardId;
    private String deviceId;
    private List<EAPReqLotInfoOB> paramList;

    public String getWaferLot() {
        return waferLot;
    }

    public void setWaferLot(String waferLot) {
        this.waferLot = waferLot;
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

    public String getTestPropram() {
        return testPropram;
    }

    public void setTestPropram(String testPropram) {
        this.testPropram = testPropram;
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

    public List<EAPReqLotInfoOB> getParamList() {
        return paramList;
    }

    public void setParamList(List<EAPReqLotInfoOB> paramList) {
        this.paramList = paramList;
    }
}
