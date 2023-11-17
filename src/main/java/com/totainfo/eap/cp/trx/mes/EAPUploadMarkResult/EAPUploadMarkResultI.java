package com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:46
 */
public class EAPUploadMarkResultI extends BaseTrxI {
    private String computerName;
    private String evtUsr;
    private String lotNo;
    private String waferId;
    private String StartingCoordinates;
    private String Result;
    private String equipmentNo;
    private String remark;
    private List<String> datas;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<String> getDatas() {
        return datas;
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
    }
}
