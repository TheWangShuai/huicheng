package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportI;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:33
 */
@Service("KVMAlarmReporting")
public class KVMAlarmReportService extends EapBaseService<KVMAlarmReportI, KVMAlarmReportO> {

    @Override
    public void mainProc(String evtNo, KVMAlarmReportI inTrx, KVMAlarmReportO outTrx) {
        String alarmCode = inTrx.getAlarmCode();
        String alarmText = inTrx.getAlarmMessage();
        String time = inTrx.getTime();
        MesHandler.alarmReport(evtNo, alarmCode, alarmText, time);

        //todo 报警需要推送给Client端展示
        ClientHandler.sendMessage(evtNo, false, 2, "设备发送报警:[" + alarmCode + "]["+ alarmText +"]");

    }
}
