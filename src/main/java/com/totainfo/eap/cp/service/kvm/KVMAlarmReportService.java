package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportI;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import org.springframework.stereotype.Service;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:33
 */
@Service("KVMAlarmReporting")
public class KVMAlarmReportService extends EapBaseService<KVMAlarmReportI, KVMAlarmReportO> {

    @Override
    public void mainProc(String uid, KVMAlarmReportI inTrx, KVMAlarmReportO outTrx) {
        EAPEqptAlarmReportI eapEqptAlarmReportI = new EAPEqptAlarmReportI();

    }
}
