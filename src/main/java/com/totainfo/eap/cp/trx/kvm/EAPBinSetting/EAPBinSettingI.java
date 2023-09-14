package com.totainfo.eap.cp.trx.kvm.EAPBinSetting;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:00
 */
public class EAPBinSettingI extends BaseTrxI {
    private List<EAPBinSettingIA> binInfo;

    public List<EAPBinSettingIA> getBinInfo() {
        return binInfo;
    }

    public void setBinInfo(List<EAPBinSettingIA> binInfo) {
        this.binInfo = binInfo;
    }
}
