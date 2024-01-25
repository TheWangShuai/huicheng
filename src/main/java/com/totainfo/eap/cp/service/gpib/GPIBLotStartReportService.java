package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("lotStartReport")
public class GPIBLotStartReportService extends EapBaseService<GPIBLotStartReportI, GPIBLotStartReportO> {
    @Resource
    private ILotDao lotDao;
    @Override
    public void mainProc(String evtNo, GPIBLotStartReportI inTrx, GPIBLotStartReportO outTrx) {
        ;
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }
        String lotNo = lotInfo.getLotId();
        if(!lotNo.equals(lotInfo.getLotId())){
            outTrx.setRtnCode("0000001");
            outTrx.setRtnMesg("GPIB上报的LotID:["+lotNo+"]与当前正在作业的LotID:["+lotInfo.getLotId()+"]不一致，请确认");
            return;
        }
        String evtUsr = lotInfo.getUserId();

        //上传生产相关信息给ems
        EMSLotInfoReportI emsLotInfoReportI = new EMSLotInfoReportI();
        emsLotInfoReportI.setLotNo(lotInfo.getLotId());
        emsLotInfoReportI.setDeviceName(lotInfo.getDevice());
        emsLotInfoReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsLotInfoReportI.setTestProgram(lotInfo.getTestProgram());
        emsLotInfoReportI.setProberCard(lotInfo.getProberCard());
        emsLotInfoReportI.setProcessState("1");
        emsLotInfoReportI.setOperator(lotInfo.getUserId());
        emsLotInfoReportI.setTemperature(lotInfo.getTemperature());
        ClientHandler.sendMessage(evtNo,false,2,"[EAP-EMS]:EAP给EMS上传开始生产相关信息指令成功");
        EmsHandler.emsLotInfoReporToEms(evtNo,emsLotInfoReportI);

        GPIBLotStartReportO gpibLotStartReportO = MesHandler.lotStart(evtNo, evtUsr, lotNo);
        if(!RETURN_CODE_OK.equals(gpibLotStartReportO.getRtnCode())){
            outTrx.setRtnCode(gpibLotStartReportO.getRtnCode());
            outTrx.setRtnMesg(gpibLotStartReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
        }
        ClientHandler.sendMessage(evtNo,false,2,"批次:[" + lotInfo.getLotId() +"] Start开始时间上报成功，制程开始");

    }
}
