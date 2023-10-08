package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportI;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.*;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.LOT_INFO_EXIST;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:33
 */
@Service("KVMAlarmReporting")
public class KVMAlarmReportService extends EapBaseService<KVMAlarmReportI, KVMAlarmReportO> {

    @Resource
    private ILotDao lotDao;

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
             return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo != null){
            outTrx.setRtnCode(LOT_INFO_EXIST);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]制程未结束，请等待");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }

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
