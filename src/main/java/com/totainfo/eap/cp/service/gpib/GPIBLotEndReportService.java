package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.KVMSlotmapMode.KVMSlotmapModeI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_RETURN_ERROR;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

@Service("lotEndReport")
public class GPIBLotEndReportService extends EapBaseService<GPIBLotEndReportI, GPIBLotEndReportO> {
    @Resource
    private ILotDao lotDao;

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${number.max}")
    private int max;

    @Override
    public void mainProc(String evtNo, GPIBLotEndReportI inTrx, GPIBLotEndReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }
        String evtUsr = lotInfo.getUserId();
        String lotNo = lotInfo.getLotId();


        // Lot End是判断是否还有Wafer的Die数据是否没有上报完成（如果逻辑正常，应该存有最后一片Wafer的数据），如果有，就将Wafer Die数据上报
        Map<String, List<DielInfo>> waferDieMap = lotInfo.getWaferDieMap();
        if(waferDieMap != null && !waferDieMap.isEmpty()){
            LogUtils.info("执行LotEnd方法开始");
            String waferId;
            List<DielInfo> dielInfos;
            EAPUploadDieResultO eapUploadDieResultO;
            for(Map.Entry<String, List<DielInfo>> entry: waferDieMap.entrySet()){
                waferId = entry.getKey();
                dielInfos = entry.getValue();
                eapUploadDieResultO = MesHandler.uploadDieResult(evtNo, lotNo, waferId, dielInfos, evtUsr);
                if (!RETURN_CODE_OK.equals(eapUploadDieResultO.getRtnCode())) {
                    outTrx.setRtnCode(eapUploadDieResultO.getRtnCode());
                    outTrx.setRtnMesg(eapUploadDieResultO.getRtnMesg());
                    ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                }
            }
            LogUtils.info("执行LotEnd方法结束");
        }
        // 上传生产相关信息给ems
        EMSLotInfoReportI emsLotInfoReportI = new EMSLotInfoReportI();
        emsLotInfoReportI.setLotNo(lotInfo.getLotId());
        emsLotInfoReportI.setDeviceName(lotInfo.getDevice());
        emsLotInfoReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsLotInfoReportI.setTestProgram(lotInfo.getTestProgram());
        emsLotInfoReportI.setProberCard(lotInfo.getProberCard());
        emsLotInfoReportI.setProcessState("4");
        emsLotInfoReportI.setOperator(lotInfo.getUserId());
        emsLotInfoReportI.setTemperature(lotInfo.getTemperature());
        ClientHandler.sendMessage(evtNo,false,2,"[EAP-EMS]: EAP给EMS上传结束生产相关信息指令成功");
        EmsHandler.emsLotInfoReporToEms(evtNo,emsLotInfoReportI);


        MesHandler.eqptStatReport(evtNo, GenergicStatDef.EqptStat.IDLE,"无",lotInfo.getUserId());
        LogUtils.info("开始给MES上报LotEnd");
        GPIBLotEndReportO gpibLotEndReportO = MesHandler.lotEnd(evtNo, evtUsr, lotNo);
        if (!RETURN_CODE_OK.equals(gpibLotEndReportO.getRtnCode())) {
            outTrx.setRtnCode(gpibLotEndReportO.getRtnCode());
            outTrx.setRtnMesg(gpibLotEndReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[Prober-EAP]: 批次号:[" + lotNo + "]制程结束");
        remove(evtNo);

    }
    public void remove(String evtNo){
        String.format("EQPT:%s:LOTINFO", equipmentNo);
        RedisHandler.remove("EQPT:state", "EQPT:%s:LOTINFO".replace("%s", equipmentNo));
        EqptInfo eqptInfo = eqptDao.getEqpt();
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        RedisHandler.remove("EQPTINFO:EQ:%s:KEY".replace("%s", equipmentNo));
    }
}

