package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.*;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.SERVICE_EXCEPTION;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:56
 */
@Service("deviceNameReport")
public class GPIBDeviceNameReportService extends EapBaseService<GPIBDeviceNameReportI, GPIBDeviceNameReportO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Override
    public void mainProc(String evtNo, GPIBDeviceNameReportI inTrx, GPIBDeviceNameReportO outTrx) {

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }
        try{
           mainProc2(evtNo, lotInfo, inTrx, outTrx);
        }catch (Exception e){
           outTrx.setRtnCode(SERVICE_EXCEPTION);
           outTrx.setRtnMesg("EAP 发送异常，请查看log");
           LogUtils.error("[{}] 异常", this.getClass().getSimpleName(), e);
        }finally {
           if(!RETURN_CODE_OK.equals(outTrx.getRtnCode())){
               EAPEndCardO eapEndCardO = KvmHandler.eapEndCard(evtNo);
               if(!RETURN_CODE_OK.equals(eapEndCardO.getRtnCode())){
                   ClientHandler.sendMessage(evtNo, false, MessageType.ERROR, eapEndCardO.getRtnMesg());
               }
               lotDao.removeLotInfo();
               stateDao.removeState();

               EqptInfo eqptInfo =  eqptDao.getEqpt();
               if(eqptInfo == null){
                   eqptInfo = new EqptInfo();
                   eqptInfo.setEqptId(GenericDataDef.equipmentNo);
                   eqptInfo.setEqptMode(EqptMode.Online);
               }
               eqptInfo.setEqptStat(EqptStat.IDLE);
               eqptDao.addEqpt(eqptInfo);
               MesHandler.eqptStatReport(evtNo, EqptStat.IDLE, "无", lotInfo.getUserId());
               RcmHandler.eqptInfoReport(evtNo, lotInfo.getLotId(), EqptStat.IDLE, _SPACE, _SPACE,_SPACE, _SPACE);
           }
        }
    }

    public void mainProc2(String evtNo, LotInfo lotInfo, GPIBDeviceNameReportI inTrx, GPIBDeviceNameReportO outTrx) {

        String lotId = lotInfo.getLotId();
        String deviceName = inTrx.getDeviceName();
        if(eapCheckName){
            String recipeId = lotInfo.getDevice();
            if(!recipeId.equals(deviceName)){
                outTrx.setRtnCode(DEVICE_DISMATCH);
                outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]Device校验失败, Device:[" + recipeId + "]，GPIB采集的Device:[" + deviceName + "]，请确认");
                return;
            }
        }
        //切换被动模式
        GPIBHandler.changeMode("++device");
        EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
        eapRepCurModelO.setRtnCode("0000000");
        eapRepCurModelO.setRtnMesg("SUCCESS");
        eapRepCurModelO.setState("0");
        ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
        ClientHandler.sendMessage(evtNo, false, 2, "GPIB切换从机模式成功！" );


        //第五步开始
        StateInfo stateInfo = stateDao.getStateInfo();
        if(stateInfo == null){
            stateInfo = new StateInfo();
            stateInfo.setLotNo(lotId);
        }
        stateInfo.setStep(StepName.FITTH);
        stateInfo.setState(StepStat.INPROCESS);
        stateDao.addStateInfo(stateInfo);

        // kvm代操参数
        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        EAPReqLotInfoOB clientLotInfo = lotDao.getClientLotInfo();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RJO");
        eapOperationInstructionI.setOpeType("C");
        List<EAPReqLotInfoOB> lotParamMap1;
        lotParamMap1 = lotInfo.getParamList();
        EAPReqLotInfoOB reqLotInfoOB = new EAPReqLotInfoOB();
        if (clientLotInfo != null){
            LogUtils.info("从Redis中取到的数据为：：[" + clientLotInfo.getParamName() + clientLotInfo.getParamValue() + "]");
            for (int i = 0; i < lotParamMap1.size(); i++) {
                if ("Sample".equals(lotParamMap1.get(i).getParamName())) {
                    reqLotInfoOB.setParamValue(clientLotInfo.getParamValue());
                    reqLotInfoOB.setParamName(clientLotInfo.getParamName());
                    lotParamMap1.set(i, reqLotInfoOB);
                }
            }
        }
        eapOperationInstructionI.setInstructList(lotParamMap1);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
        if (StringUtils.isEmpty(returnMesg)) {
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP发送代操采集指令成功。");
    }
}
