package com.totainfo.eap.cp.trx.client.EAPLotIdRead;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 14:34
 */
public class EAPLotIdReadI extends BaseTrxI {
    private String userId;
    private String lotNo;
    private String proberCardId;
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
