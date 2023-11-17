package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportI;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.*;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.LOT_INFO_EXIST;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._TRUE;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:33
 */
@Service("KVMAlarmReporting")
public class KVMAlarmReportService extends EapBaseService<KVMAlarmReportI, KVMAlarmReportO> {

    @Resource
    private ILotDao lotDao;

    private String stop;
    @Override
    public void mainProc(String evtNo, KVMAlarmReportI inTrx, KVMAlarmReportO outTrx) {
        String alarmCode = inTrx.getAlarmCode();
        String alarmText = inTrx.getAlarmMessage();
        String time = inTrx.getTime();
        MesHandler.alarmReport(evtNo, alarmCode, alarmText, time);

        //报警需要推送给Client端展示
        ClientHandler.sendMessage(evtNo, false, 2, "设备发送报警:[" + alarmCode + "]["+ alarmText +"]");

        //判断是不是制程结束的报警，如果是，发给MES Check Out
        if(!"00405".equals(alarmCode)){
            EAPEqptAlarmReportO eapEqptAlarmReportO = MesHandler.alarmReport(evtNo, alarmCode, alarmText, time);
            String s = eapEqptAlarmReportO.toString();
            ClientHandler.sendMessage(evtNo,false,2,s);
            return;
        }
        LotInfo lotInfo = lotDao.getCurLotInfo();
        //发送量测数据是否齐全的请求
        EAPReqMeasureResultO eapReqMeasureResultO = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
        stop = eapReqMeasureResultO.getRtnMesg();
        while (!_TRUE.equals(stop)){
            EAPReqMeasureResultO Msg = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
            stop = Msg.getRtnMesg();
        }
        ClientHandler.sendMessage(evtNo,false,2,"量测结果均以生成");
        EAPReqCheckOutO eapReqCheckOutO = MesHandler.checkOutReq(evtNo, lotInfo.getLotId());
        if(!RETURN_CODE_OK.equals(eapReqCheckOutO.getRtnCode())){
            outTrx.setRtnCode(eapReqCheckOutO.getRtnCode());
            outTrx.setRtnMesg(eapReqCheckOutO.getRtnMesg());
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo,false,2,"批次:[" + lotInfo.getLotId() +"] Check Out 成功");
    }
}
