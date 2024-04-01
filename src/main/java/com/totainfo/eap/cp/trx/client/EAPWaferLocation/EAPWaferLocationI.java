package com.totainfo.eap.cp.trx.client.EAPWaferLocation;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/3/22
 */
public class EAPWaferLocationI extends BaseTrxI {

    private String userId;
    private String lotNo;
    private String proberCardId;
    private String sampleValue;
    private String rtnCode;
    private String rtnMesg;
    private int selectType;
    private String datas;

    public int getSelectType() {
        return selectType;
    }

    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }

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

    public String getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getProberCardId() {
        return proberCardId;
    }

    public void setProberCardId(String proberCardId) {
        this.proberCardId = proberCardId;
    }
}