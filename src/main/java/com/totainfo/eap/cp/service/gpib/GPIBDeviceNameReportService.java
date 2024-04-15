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
import com.totainfo.eap.cp.entity.ValidationInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.mode.ValidationItem;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInI;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import com.totainfo.eap.cp.util.ValidationUtil;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
    private ClientHandler clientHandler;

    @Resource
    private IStateDao stateDao;

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Value("${spring.rabbitmq.rms.checkFlag}")
    private boolean rmsCheckFlag;

    private Lock lock;

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

    public void mainProc2(String evtNo, LotInfo lotInfo, GPIBDeviceNameReportI inTrx, GPIBDeviceNameReportO outTrx) throws DocumentException {

        String lotId = lotInfo.getLotId();
        String deviceName = inTrx.getDeviceName();
        String recipeId = "";
        if(eapCheckName){
            recipeId = lotInfo.getDevice();
            if(!recipeId.equals(deviceName)){
                outTrx.setRtnCode(DEVICE_DISMATCH);
                outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]Device校验失败, Device:[" + recipeId + "]，GPIB采集的Device:[" + deviceName + "]，请确认");
                //切换被动模式
                EAPRepCurModelO eapRepCurModelO = getCurModelO();
                ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
//                ClientHandler.sendMessage(evtNo, false, 2, "GPIB切换从机模式成功！" );
                return;
            }
        }

        // DEVICENAME校验成功
        ClientHandler.sendMessage(evtNo, false, 2, "DeviceName校验结果: 机台采集[" + deviceName + "],MES下发:[" + recipeId + "]");
        //切换被动模式
        EAPRepCurModelO eapRepCurModelO = getCurModelO();
        ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
//        ClientHandler.sendMessage(evtNo, false, 2, "GPIB切换从机模式成功！" );

        // DEVICE参数校验
        if (rmsCheckFlag) {
            LogUtils.info("开始发给rms做校验请求");
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-RMS]: Device:[" + lotInfo.getDevice() + "]发送RMS请求参数校验, 等待结果");
            RmsOnlineValidationO rmsOnlineValidationO = RMSHandler.toRmsOnlineValidation(evtNo, equipmentNo, lotInfo.getLotId(), lotInfo.getDevice(),"CP");
            if (rmsOnlineValidationO == null) {
                outTrx.setRtnCode(RMS_TIME_OUT);
                outTrx.setRtnMesg("[EAP-RMS]:EAP 发送Device:[" + lotInfo.getDevice() + "]验证请求，RMS没有回复");;
                return;
            }
            if (!RMSResult.TRUE.equals(rmsOnlineValidationO.getResult())) {
                Stateset("4", "3", lotId);
                outTrx.setRtnCode(RMS_FAILD);
                outTrx.setRtnMesg("[EAP-RMS]:Device:[" + lotInfo.getDevice() + "]验证失败，原因:[" + rmsOnlineValidationO.getReason() + "]");
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-RMS]:Device:[" + lotInfo.getDevice() + "]已激活, 参数内容一致, RMS验证成功");
        }

        EmsHandler.reportRunWorkInfo(evtNo,"DeviceName参数校验完成",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());

        // 第四步Device参数验证完成
        clientHandler.setFlowStep(StepName.FOURTH,StepStat.COMP);

        // step5 查询需要校验的参数的当前值,并放到info里
//        List<ValidationItem> validationItemList = ValidationUtil.getValidationItemList();
//
//        List<String> paramIdList = validationItemList.stream().map(ValidationItem::getParamId).collect(Collectors.toList());
//        for (String paramId : paramIdList) {
//            while (ValidationInfo.replyFlag){
//                GPIBHandler.getParamValue(paramId);
//                ValidationInfo.replyFlag=false;
//            }
//        }
//        for (ValidationItem validationItem : validationItemList) {
//            // todo 循环 查询gpib参数
//            String paramValue = "";
//
//            validationItem.setActualValue(paramValue);
//        }
//        ValidationInfo.setValidationItemList(validationItemList);

        //发送Lot Setting
        //第五步下发LotSetting信息开始
        StateInfo stateInfo = stateDao.getStateInfo();
        clientHandler.setFlowStep(StepName.FITTH,StepStat.INPROCESS);
        //将信息下发给KVM
        EAPLotInfoWriteInI eapLotInfoWriteInI = getEapLotInfoWriteInI(lotInfo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapLotInfoWriteInI);
        if (StringUtils.isEmpty(returnMesg)) {
            stateInfo.setState(StepStat.FAIL);
            stateDao.addStateInfo(stateInfo);
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM没有回复");
            return;
        }
        EAPLotInfoWriteInO eapLotInfoWriteInO = JacksonUtils.string2Object(returnMesg, EAPLotInfoWriteInO.class);
        if (!RETURN_CODE_OK.equals(eapLotInfoWriteInO.getRtnCode())) {
            stateInfo.setState(StepStat.FAIL);
            stateDao.addStateInfo(stateInfo);
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            outTrx.setRtnCode(eapLotInfoWriteInO.getRtnCode());
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM返回失败，原因:[" + eapLotInfoWriteInO.getRtnMesg() + "]");
            return;
        }
        //发送给前端，LOT信息发送KVM成功
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:批次:[" + lotInfo.getLotId() + "]开始进行自动化作业");
        MesHandler.eqptStatReport(evtNo, EqptStat.RUN, "无", lotInfo.getUserId());
        RcmHandler.eqptInfoReport(evtNo, lotInfo.getLotId(), EqptStat.RUN, _SPACE, _SPACE,_SPACE, _SPACE);
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:批次:[" + lotInfo.getLotId() + "], LotSetting信息写入成功");
        EmsHandler.reportRunWorkInfo(evtNo,"批次信息写入成功",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
        //第五步下发LotSetting信息结束
        clientHandler.setFlowStep(StepName.FITTH,StepStat.COMP);
    }

    private static EAPLotInfoWriteInI getEapLotInfoWriteInI(LotInfo lotInfo) {
        EAPLotInfoWriteInI eapLotInfoWriteInI = new EAPLotInfoWriteInI();
        eapLotInfoWriteInI.setTrxId("EAPACCEPT");
        eapLotInfoWriteInI.setActionFlg("RJI");
        eapLotInfoWriteInI.setUserId(lotInfo.getUserId());
        eapLotInfoWriteInI.setProberCardId(lotInfo.getProberCard());
        eapLotInfoWriteInI.setLoadBoardId(lotInfo.getLoadBoardId());
        eapLotInfoWriteInI.setWaferLot(lotInfo.getWaferLot());
        eapLotInfoWriteInI.setDeviceId(lotInfo.getDeviceId());
        eapLotInfoWriteInI.setTestProgram(lotInfo.getTestProgram());
        return eapLotInfoWriteInI;
    }

    private static EAPRepCurModelO getCurModelO() {
        //切换被动模式
        GPIBHandler.changeMode("++device");
        EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
        eapRepCurModelO.setRtnCode("0000000");
        eapRepCurModelO.setRtnMesg("SUCCESS");

        eapRepCurModelO.setState("0");
        return eapRepCurModelO;
    }

    public void removeCache() {
        lotDao.removeLotInfo();
        stateDao.removeState();
    }


    public void Stateset(String step, String state, String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }
}
