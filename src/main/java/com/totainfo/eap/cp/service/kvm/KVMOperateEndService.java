package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.KVMOperateState;
import com.totainfo.eap.cp.commdef.GenergicStatDef.RMSResult;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.RMSHandler;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;
import static com.totainfo.eap.cp.commdef.GenericDataDef.testerUrl;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 9:31
 */
@Service("EAPACCEPT")
public class KVMOperateEndService extends EapBaseService<KVMOperateEndI, KVMOperateEndO> {


    @Resource
    private ILotDao lotDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.rms.checkFlag}")
    private boolean rmsCheckFlag;

    @Override
    public void mainProc(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        String opeType = inTrx.getOpeType();
        switch (opeType){
            case "A":  //Prober LOT信息写入后，代操完成
                sendDeviceCollection(evtNo, inTrx, outTrx);
                break;
            case "B":  //Device Name 采集完成
                deviceNameVerfication(evtNo, inTrx, outTrx);
                break;
            case "C":   //模式设置， 报警等设置完成
                deviceParamCollection(evtNo, inTrx, outTrx);
                break;
            case "D":   //Device Param 采集完成
                toRmsVerificationDevice(evtNo, inTrx, outTrx);
                break;
            case "E":  // Device 温度采集
                deviceTemperayureCollection(evtNo,inTrx,outTrx);
            case "F":   //Test 测试程序清除，Function key清除完成
                testProgramVerification(evtNo, inTrx, outTrx);
                break;
        }
    }


    /**
     *
     * @param evtNo
     * @param inTrx
     * @param outTrx
     * 下发采集Device Name，并与Lot Device Name进行比对
     */
    private void sendDeviceCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){

        ClientHandler.sendMessage(evtNo, false,2, "KVM 批次信息写入成功.");

        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
        eapSingleParamCollectionI.setActionFlg("RWPE");
        eapSingleParamCollectionI.setParameterName("");
        eapSingleParamCollectionI.setRequestKey(evtNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 发送采集Device Name， KVM 没有返回");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
        if(!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 发送采集Device Name， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }

        ClientHandler.sendMessage(evtNo, false,2, "EAP发送Device name采集指令成功。");
    }


    private void deviceNameVerfication(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){
         String state = inTrx.getState();
         if(KVMOperateState.Fail.equals(state)){
             outTrx.setRtnCode(KVM_RETURN_ERROR);
             outTrx.setRtnMesg("KVM采集Device Name完成， 状态Error.");
             return;
         }
         String responseKey = inTrx.getResponseKey();
         String eqptDeviceName = inTrx.getOpeContent();
         List<String> recipeIds = new ArrayList<>(1);
         recipeIds.add(eqptDeviceName);
         AsyncUtils.setResponse(responseKey, recipeIds);

         LotInfo lotInfo = lotDao.getCurLotInfo();
         if(lotInfo == null){
             return;
         }

         String lotDeviceName = lotInfo.getDevice();
         if(!lotDeviceName.equals(eqptDeviceName)){
             outTrx.setRtnCode(DEVICE_DISMATCH);
             outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]Device:["+ lotDeviceName + "]与KVM采集的Device:[" + eqptDeviceName + "]不匹配，请确认");
             ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
             return;
         }
         ClientHandler.sendMessage(evtNo, false, 2, "KVM Device Name采集成功，设备Device Name:["+ eqptDeviceName +"], 批次Device:[" + lotDeviceName + "]");

         EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
         eapOperationInstructionI.setTrxId("EAPACCEPT");
         eapOperationInstructionI.setTrypeId("I");
         eapOperationInstructionI.setActionFlg("RJO");
         eapOperationInstructionI.setOpeType("C");
         Map<String, String> lotParamMap = lotInfo.getParamMap();
         List<EAPOperationInstructionIA> lotParamMap1=lotInfo.getParamList();

//         List<EAPOperationInstructionIA> eapOperationInstructionIAS = new ArrayList<>(lotParamMap1.size());
//         for(Map.Entry<String, String> entry: lotParamMap.entrySet()){
//             EAPOperationInstructionIA eapOperationInstructionIA = new EAPOperationInstructionIA();
//             eapOperationInstructionIA.setInstructKey(entry.getKey());
//             eapOperationInstructionIA.setInstructValue(entry.getValue());
//             eapOperationInstructionIAS.add(eapOperationInstructionIA);
//         }
         eapOperationInstructionI.setInstructList(lotParamMap1);
         String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
         if(StringUtils.isEmpty(returnMesg)){
             outTrx.setRtnCode(KVM_TIME_OUT);
             outTrx.setRtnMesg("EAP 下发代操指令， KVM 没有返回");
             return;
         }
         EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
         if(!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())){
             outTrx.setRtnCode(KVM_RETURN_ERROR);
             outTrx.setRtnMesg("EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
             return;
         }
         ClientHandler.sendMessage(evtNo, false, 2, "EAP发送代操采集指令成功。");
    }

    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx
     * 模式设置，Fail报警，模式选择等代操完成
     */
    private void deviceParamCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){

        String state = inTrx.getState();
        if(KVMOperateState.Fail.equals(state)){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM代操完成， 状态Error.");
            return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }


        ClientHandler.sendMessage(evtNo, false, 2, "KVM 代操完成, EAP发送RMS进行验证。");

        if(rmsCheckFlag){
            RmsOnlineValidationO rmsOnlineValidationO = RMSHandler.toRmsOnlineValidation(evtNo, equipmentNo, lotInfo.getLotId(), lotInfo.getDevice());
            if(rmsOnlineValidationO == null){
                outTrx.setRtnCode(RMS_TIME_OUT);
                outTrx.setRtnMesg("EAP 发送Device:[" + lotInfo.getDevice() + "]验证请求，RMS没有回复");
                ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
                return;
            }
            if(!RMSResult.TRUE.equals(rmsOnlineValidationO.getResult())){
                outTrx.setRtnCode(RMS_FAILD);
                outTrx.setRtnMesg("Device:[" + lotInfo.getDevice() + "]验证失败，原因:[" + rmsOnlineValidationO.getReason() + "]");
                ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
                return;
            }
        }
        ClientHandler.sendMessage(evtNo, false, 2, "Device:[" + lotInfo.getDevice() + "] RMS验证成功。");

        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RWPEE");
        eapOperationInstructionI.setOpeType("E");
        Map<String, String> lotParamMap = lotInfo.getParamMap();
        List<EAPOperationInstructionIA> lotParamMap1=lotInfo.getParamList();
//        List<EAPOperationInstructionIA> eapOperationInstructionIAS = new ArrayList<>(lotParamMap.size());
//        for(Map.Entry<String, String> entry: lotParamMap.entrySet()){
//            EAPOperationInstructionIA eapOperationInstructionIA = new EAPOperationInstructionIA();
//            eapOperationInstructionIA.setInstructKey(entry.getKey());
//            eapOperationInstructionIA.setInstructValue(entry.getValue());
//            eapOperationInstructionIAS.add(eapOperationInstructionIA);
//        }
        eapOperationInstructionI.setInstructList(lotParamMap1);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, testerUrl, eapOperationInstructionI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 下发清除测试程式， KVM 没有返回");
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if(!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 下发清除测试程式， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            return;
        }
//        ClientHandler.sendMessage(evtNo, false, 2, "Device:[" + lotInfo.getDevice() + "] RMS验证成功。");
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送清除测试程式指令成功。");
    }

    /**
     *
     * @param evtNo
     * @param inTrx
     * @param outTrx
     * Device Param 采集完成
     */
    private void toRmsVerificationDevice(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){
        String state = inTrx.getState();
        if(KVMOperateState.Fail.equals(state)){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM采集Device param完成， 状态Error.");
            return;
        }
        String responseKey = inTrx.getResponseKey();
        String recipeBody = inTrx.getOpeContent();
        AsyncUtils.setResponse(responseKey, recipeBody);
        String eqptDeviceName = inTrx.getOpeContent();
        List<String> recipeIds = new ArrayList<>(1);
        recipeIds.add(eqptDeviceName);
        AsyncUtils.setResponse(responseKey, recipeIds);

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }

        ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device param完成。");
        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RWPEE");
        Map<String, String> lotParamMap = lotInfo.getParamMap();
        List<EAPOperationInstructionIA> lotParamMap1=lotInfo.getParamList();

//         List<EAPOperationInstructionIA> eapOperationInstructionIAS = new ArrayList<>(lotParamMap1.size());
//         for(Map.Entry<String, String> entry: lotParamMap.entrySet()){
//             EAPOperationInstructionIA eapOperationInstructionIA = new EAPOperationInstructionIA();
//             eapOperationInstructionIA.setInstructKey(entry.getKey());
//             eapOperationInstructionIA.setInstructValue(entry.getValue());
//             eapOperationInstructionIAS.add(eapOperationInstructionIA);
//         }
        eapOperationInstructionI.setInstructList(lotParamMap1);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 下发代操指令， KVM 没有返回");
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if(!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送代操采集指令成功。");
    }
    private void deviceTemperayureCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String state = inTrx.getState();
        if(KVMOperateState.Fail.equals(state)){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM第三步完成， 状态Error.");
            return;
        }
        String responseKey = inTrx.getResponseKey();
        String eqptDeviceTemperature = inTrx.getTemperature();
        List<String> recipeIds = new ArrayList<>(1);
        recipeIds.add(eqptDeviceTemperature);
        AsyncUtils.setResponse(responseKey, recipeIds);

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }
        String lotDeviceTemperature = lotInfo.getTemperature();
        if(!lotDeviceTemperature.equals(eqptDeviceTemperature)){
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]温度:["+ lotDeviceTemperature + "]与KVM采集的温度:[" + eqptDeviceTemperature + "]不匹配，请确认");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "KVM Device 温度采集成功，设备Device 温度:["+ eqptDeviceTemperature +"], 批次Device的温度:[" + lotDeviceTemperature + "]");
        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RPS");
        Map<String, String> lotParamMap = lotInfo.getParamMap();
        List<EAPOperationInstructionIA> eapOperationInstructionIAS = new ArrayList<>(lotParamMap.size());
        for(Map.Entry<String, String> entry: lotParamMap.entrySet()){
            EAPOperationInstructionIA eapOperationInstructionIA = new EAPOperationInstructionIA();
            eapOperationInstructionIA.setInstructKey(entry.getKey());
            eapOperationInstructionIA.setInstructValue(entry.getValue());
            eapOperationInstructionIAS.add(eapOperationInstructionIA);
        }

        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 没有返回");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if(!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送清除测试程式指令成功。");
    }

    private void testProgramVerification(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){
        String state = inTrx.getState();
        if(KVMOperateState.Fail.equals(state)){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM 清除测试程式完成， 状态Error.");
            return;
        }
        String eqpTestProgram = inTrx.getOpeContent();
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        String lotTestProgram = lotInfo.getTestProgram();
        if(!lotTestProgram.equals(eqpTestProgram)){
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "] TestProgram:["+ lotTestProgram + "]与KVM采集的TestProgram:[" + eqpTestProgram + "]不匹配，请确认");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, true, 2, "KVM测试程式验证通过，请开始作业");
    }
}
