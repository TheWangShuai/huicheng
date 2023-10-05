package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:14
 */
public class EAPReqLotInfoOA {
    private String waferLot;
    private String device;
    private String probeCard;
    private String testProgram;
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

    public String getProbeCard() {
        return probeCard;
    }

    public void setProbeCard(String probeCard) {
        this.probeCard = probeCard;
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

    public List<EAPReqLotInfoOB> getParamList() {
        return paramList;
    }

    public void setParamList(List<EAPReqLotInfoOB> paramList) {
        this.paramList = paramList;
    }
}
