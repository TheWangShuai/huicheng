package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.trx.kvm.KVMStatusReport.KVMStatusReportI;
import com.totainfo.eap.cp.trx.kvm.KVMStatusReport.KVMStatusReportO;
import org.springframework.stereotype.Service;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 13:55
 */
@Service("KVMEquipmentState")
public class KVMStatusReportService extends EapBaseService<KVMStatusReportI, KVMStatusReportO> {

    @Override
    public void mainProc(String evtNo, KVMStatusReportI inTrx, KVMStatusReportO outTrx) {


    }
}
