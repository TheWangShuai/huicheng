package com.totainfo.eap.cp.trx.rcm;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author WangShuai
 * @date 2024/4/2
 */
public class EapReportMESDataInfoI extends BaseTrxI {

    private String mesData;

    public String getMesData() {
        return mesData;
    }

    public void setMesData(String mesData) {
        this.mesData = mesData;
    }
}