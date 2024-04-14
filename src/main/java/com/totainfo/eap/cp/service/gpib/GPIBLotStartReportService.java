package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

@Service("lotStartReport")
public class GPIBLotStartReportService extends EapBaseService<GPIBLotStartReportI, GPIBLotStartReportO> {
    @Resource
    private ILotDao lotDao;

    @Resource
    private StateDao stateDao;
    @Override
    public void mainProc(String evtNo, GPIBLotStartReportI inTrx, GPIBLotStartReportO outTrx) {
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
        EMSLotInfoReportO emsLotInfoReportO = EmsHandler.emsLotInfoReporToEms(evtNo, emsLotInfoReportI);
        if (!RETURN_CODE_OK.equals(emsLotInfoReportO.getRtnCode())){
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            ClientHandler.sendMessage(evtNo,false,1,"[EAP-Client]: " + emsLotInfoReportO.getRtnMesg());
            return;
        }

        GPIBLotStartReportO gpibLotStartReportO = MesHandler.lotStart(evtNo, evtUsr, lotNo);
        if(!RETURN_CODE_OK.equals(gpibLotStartReportO.getRtnCode())){
            outTrx.setRtnCode(gpibLotStartReportO.getRtnCode());
            outTrx.setRtnMesg(gpibLotStartReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo,false,2,"批次:[" + lotInfo.getLotId() +"] ： 制程开始");
    }
    public void removeCache(){
        lotDao.removeLotInfo();
        stateDao.removeState();

    }
}
