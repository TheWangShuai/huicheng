package com.totainfo.eap.cp.entity;

import com.totainfo.eap.cp.trx.client.EAPReportDieInfo.DieInfoOA;

import java.util.List;

public class DieCountInfo {

    List<DieInfoOA> dieInfoOAS;

    public List<DieInfoOA> getDieInfoOAS() {
        return dieInfoOAS;
    }

    public void setDieInfoOAS(List<DieInfoOA> dieInfoOAS) {
        this.dieInfoOAS = dieInfoOAS;
    }
}
