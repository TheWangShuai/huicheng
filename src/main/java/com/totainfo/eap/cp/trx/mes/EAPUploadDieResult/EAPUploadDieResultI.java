package com.totainfo.eap.cp.trx.mes.EAPUploadDieResult;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultI;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultIA;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:42
 */
public class EAPUploadDieResultI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String lotNo;
    private String waferId;
    private String StartingCoordinates;
    private String Result;
    private String equipmentNo;

    private List<EAPUploadDieResultIA> datas;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getWaferId() {
        return waferId;
    }

    public void setWaferId(String waferId) {
        this.waferId = waferId;
    }

    public String getStartingCoordinates() {
        return StartingCoordinates;
    }

    public void setStartingCoordinates(String startingCoordinates) {
        StartingCoordinates = startingCoordinates;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public List<EAPUploadDieResultIA> getDatas() {
        return datas;
    }

    public void setDatas(List<EAPUploadDieResultIA> datas) {
        this.datas = datas;
    }
}
