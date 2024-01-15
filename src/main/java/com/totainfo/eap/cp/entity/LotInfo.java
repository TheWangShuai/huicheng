package com.totainfo.eap.cp.entity;


import java.util.List;
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
    private String waferLot;
    private String temperatureRange;
    private String temperature;
    private int dieCount;

    private Map<String, String> paramList;
    private List paramList1;

    private Map<String, List<DielInfo>> waferDieMap;

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
        return paramList;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramList = paramMap;
    }

    public String getWaferLot() {
        return waferLot;
    }

    public void setWaferLot(String waferLot) {
        this.waferLot = waferLot;
    }

    public List getParamList() {
        return paramList1;
    }

    public void setParamList(List paramList1) {
        this.paramList1 = paramList1;
    }

    public String getTemperatureRange() {
        return temperatureRange;
    }

    public void setTemperatureRange(String temperatureRange) {
        this.temperatureRange = temperatureRange;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setParamList(Map<String, String> paramList) {
        this.paramList = paramList;
    }

    public List getParamList1() {
        return paramList1;
    }

    public void setParamList1(List paramList1) {
        this.paramList1 = paramList1;
    }

    public Map<String, List<DielInfo>> getWaferDieMap() {
        return waferDieMap;
    }

    public void setWaferDieMap(Map<String, List<DielInfo>> waferDieMap) {
        this.waferDieMap = waferDieMap;
    }

    public int getDieCount() {
        return dieCount;
    }

    public void setDieCount(int dieCount) {
        this.dieCount = dieCount;
    }
}
