package com.totainfo.eap.cp.service.kvm;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.commdef.GenergicStatDef.KVMOperateState;
import com.totainfo.eap.cp.commdef.GenergicStatDef.RMSResult;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.service.client.EAPEqpControlService;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandO;
import com.totainfo.eap.cp.trx.kvm.EAPDeviceParamCollection.EAPDeviceParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionO;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportI;
import com.totainfo.eap.cp.trx.kvm.KVMAlarmReport.KVMAlarmReportO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultO;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.backoff.Sleeper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.*;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;
import static com.totainfo.eap.cp.commdef.GenericDataDef.testerUrl;


import org.json.JSONObject;


/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 9:31
 */
@Service("EAPACCEPT")
public class KVMOperateEndService extends EapBaseService<KVMOperateEndI, KVMOperateEndO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IEqptDao iEqptDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.rms.checkFlag}")
    private boolean rmsCheckFlag;

    private Object lock = new Object();

    private static String str;

    private static String dn;

    private String stop;

    @Override
    public void mainProc(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String actionFlg = inTrx.getActionFlg();
        if (actionFlg.equals("SCM")) {
            //KVM和eap同步连线状态
            EqptInfo eqptInfo = iEqptDao.getEqpt();
            LotInfo curLotInfo = lotDao.getCurLotInfo();
            if (curLotInfo == null) {
                curLotInfo = new LotInfo();
                curLotInfo.setUserId("null");
                curLotInfo.setLotId("null");
                curLotInfo.setProberCard("null");
            }
            EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
            eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
            eapSyncEqpInfoI.setActionFlg("RLC");
            eapSyncEqpInfoI.setUserId(curLotInfo.getUserId());
            eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
            eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
            eapSyncEqpInfoI.setLotNo(curLotInfo.getLotId());
            eapSyncEqpInfoI.setProberCardId(curLotInfo.getProberCard());

            ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);

            outTrx.setTrxId("KVMSyncConnectionMode");
            outTrx.setModel(eqptInfo.getEqptMode());
            outTrx.setRtnMesg(RETURN_MESG_OK);
            return;

        } else if (actionFlg.equals("RRTU")) {
            //KVM报警信息上报
            String alarmCode = inTrx.getAlarmCode();
            String alarmText = inTrx.getAlarmMessage();
            String time = inTrx.getTime();

            //判断是不是制程结束的报警，如果是，发给MES Check Out
            if (!"O405".equals(alarmCode)) {
                MesHandler.alarmReport(evtNo, alarmCode, alarmText, time);
                ClientHandler.sendMessage(evtNo, false, 2, "设备发送报警:[" + alarmCode + "][" + alarmText + "]");
                return;
            }

            LotInfo lotInfo = lotDao.getCurLotInfo();
            //发送量测数据是否齐全的请求
            EAPReqMeasureResultO eapReqMeasureResultO = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
            stop = eapReqMeasureResultO.getRtnCode();
            while (!RETURN_CODE_OK.equals(stop)){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                EAPReqMeasureResultO Msg = MesHandler.measureResultReq(evtNo,"PP03D34505-01");
                stop = Msg.getRtnCode();
            }
            ClientHandler.sendMessage(evtNo,false,2,"量测结果均以生成");

            EAPReqCheckOutO eapReqCheckOutO = MesHandler.checkOutReq(evtNo, "PP03D34505-01");
            if (!RETURN_CODE_OK.equals(eapReqCheckOutO.getRtnCode())) {
                outTrx.setRtnCode(eapReqCheckOutO.getRtnCode());
                outTrx.setRtnMesg(eapReqCheckOutO.getRtnMesg());
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + "PP03D34505-01" + "] Check Out 成功");
        } else if (actionFlg.equals("REE")) {
            //KVM状态上报
            String eqptStat = inTrx.getState();
            EqptInfo eqptInfo = iEqptDao.getEqptWithLock();
            if (eqptInfo == null) {
                eqptInfo = new EqptInfo();
                eqptInfo.setEqptId(GenericDataDef.equipmentNo);
                eqptInfo.setEqptMode(EqptMode.Offline);
            }

            //状态没变不做处理
            if (eqptStat.equals(eqptInfo.getEqptStat())) {
                return;
            }
            eqptInfo.setEqptStat(eqptStat);
            iEqptDao.addEqpt(eqptInfo);

            MesHandler.eqptStatReport(evtNo, eqptStat, "");

            EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
            eapSyncEqpInfoI.setTrxId("ConnectInfo");
            eapSyncEqpInfoI.setTrypeId("I");
            eapSyncEqpInfoI.setActionFlg("RLC");
            eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
            eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());

            LotInfo lotInfo = lotDao.getCurLotInfo();
            if (lotInfo != null) {
                eapSyncEqpInfoI.setUserId(lotInfo.getUserId());
                eapSyncEqpInfoI.setLotNo(lotInfo.getLotId());
                eapSyncEqpInfoI.setProberCardId(lotInfo.getProberCard());
            }
            ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        } else {
            String opeType = inTrx.getOpeType();
            switch (opeType) {
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
                case "E":  // Device 温度采集完成
                    deviceTemperatureCollection(evtNo, inTrx, outTrx);
                    break;
                case "F":   //Test 测试程序清除，Function key清除完成
                    testProgramVerification(evtNo, inTrx, outTrx);
                case "G":   // 实时检验温度
                    realTimeTemperatureCollection(evtNo,inTrx,outTrx);
                    break;
            }
        }
    }

    private void realTimeTemperatureCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM采集Device 实时温度完成， 状态Error.");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        String eqptDeviceTemperature = inTrx.getOpeContent();
        JSONObject jsonObject = new JSONObject(eqptDeviceTemperature);
        String valueInJson = jsonObject.getString("");
        // 使用正则表达式提取数字部分
        String numericPart = valueInJson.replaceAll("[^\\d.]", "");
        ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device 实时温度完成。");

        String temperatureRang = lotInfo.getTemperatureRange();
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = lotInfo.getTemperature();
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if (value <= tem - tempRang || value >= tem + tempRang) {
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "KVM Device 实时温度采集校验成功，设备Device 温度:[" + numericPart + "], 批次Device的温度:[" + temperature + "],在该温度范围内");

    }


    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx 下发采集Device Name，并与Lot Device Name进行比对
     */
    private void sendDeviceCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String lotId = lotInfo.getLotId();
        Stateset("2","2",lotId);
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM 批次信息写入成功.");

        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
        eapSingleParamCollectionI.setActionFlg("RWPE");
        eapSingleParamCollectionI.setParameterName("");
        eapSingleParamCollectionI.setRequestKey(evtNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
        Stateset("3","1",lotId);
        if (StringUtils.isEmpty(returnMesg)) {
            Stateset("3","3",lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 发送采集Device Name， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
        if (!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())) {
            Stateset("3","3",lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 发送采集Device Name， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP发送Device name采集指令成功。");
    }


    private void deviceNameVerfication(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:KVM采集Device Name完成， 状态Error.");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String responseKey = inTrx.getResponseKey();
        String eqptDeviceName = inTrx.getOpeContent();
        dn = inTrx.getOpeContent();
        if (AsyncUtils.existRequest(responseKey)) {
            List<String> recipeIds = new ArrayList<>(1);
            recipeIds.add(eqptDeviceName);
            AsyncUtils.setResponse(responseKey, recipeIds);
        } else {
            String lotId = lotInfo.getLotId();
            String lotDeviceName = lotInfo.getDevice();
            if (!lotDeviceName.equals(eqptDeviceName)) {
                Stateset("3","3",lotId);
                outTrx.setRtnCode(DEVICE_DISMATCH);
                outTrx.setRtnMesg("[EAP-KVM]:批次:[" + lotInfo.getLotId() + "]校验失败Device:[" + lotDeviceName + "]，KVM采集的Device:[" + eqptDeviceName + "]，请确认");
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM Device Name采集校验成功，设备Device Name:[" + eqptDeviceName + "], 批次Device:[" + lotDeviceName + "]");
            Stateset("3","2",lotId);
            EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
            eapOperationInstructionI.setTrxId("EAPACCEPT");
            eapOperationInstructionI.setTrypeId("I");
            eapOperationInstructionI.setActionFlg("RJO");
            eapOperationInstructionI.setOpeType("C");
            Map<String, String> lotParamMap = lotInfo.getParamMap();
            List<EAPOperationInstructionIA> lotParamMap1 = lotInfo.getParamList();

//         List<EAPOperationInstructionIA> eapOperationInstructionIAS = new ArrayList<>(lotParamMap1.size());
//         for(Map.Entry<String, String> entry: lotParamMap.entrySet()){
//             EAPOperationInstructionIA eapOperationInstructionIA = new EAPOperationInstructionIA();
//             eapOperationInstructionIA.setInstructKey(entry.getKey());
//             eapOperationInstructionIA.setInstructValue(entry.getValue());
//             eapOperationInstructionIAS.add(eapOperationInstructionIA);
//         }
            eapOperationInstructionI.setInstructList(lotParamMap1);
            String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
            Stateset("4","1",lotId);
            if (StringUtils.isEmpty(returnMesg)) {
                Stateset("4","3",lotId);
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 没有返回");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
            if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
                Stateset("4","3",lotId);
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP发送代操采集指令成功。");
        }
    }

    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx 模式设置，Fail报警，模式选择等代操完成
     */
    private void deviceParamCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:KVM代操完成， 状态Error.");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }

        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM 代操完成。");
        String lotId = lotInfo.getLotId();
        Stateset("4","2",lotId);
        if (rmsCheckFlag) {
            EAPDeviceParamCollectionI eapDeviceParamCollectionI = new EAPDeviceParamCollectionI();
            eapDeviceParamCollectionI.setTrxId("EAPACCEPT");
            eapDeviceParamCollectionI.setActionFlg("RWPEE");
            eapDeviceParamCollectionI.setDeviceName("");
            eapDeviceParamCollectionI.setRequestKey(evtNo);
            String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapDeviceParamCollectionI);
            if (StringUtils.isEmpty(returnMesg)) {
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("EAP 下发Device param采集指令， KVM 没有返回");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
            if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("EAP 下发Device param采集指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "EAP发送Device param采集指令成功。");
        }
        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
        eapSingleParamCollectionI.setActionFlg("RPS");
        eapSingleParamCollectionI.setParameterName("");
        eapSingleParamCollectionI.setRequestKey(evtNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
        Stateset("5","1",lotId);
        if (StringUtils.isEmpty(returnMesg)) {
            Stateset("5","3",lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
        if (!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())) {
            Stateset("5","3",lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送Device 温度采集指令成功。");
    }


    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx Device Param 采集完成
     */
    private void toRmsVerificationDevice(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        synchronized (lock) {
            String state = inTrx.getState();
            if (KVMOperateState.Fail.equals(state)) {
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("KVM采集Device param完成， 状态Error.");
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }

            String responseKey = inTrx.getResponseKey();
            String recipeBody = inTrx.getOpeContent();
            if (AsyncUtils.existRequest(responseKey)) {
                AsyncUtils.setResponse(responseKey, recipeBody);
            } else {
                ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device param完成。");
                if (rmsCheckFlag) {
                    RmsOnlineValidationO rmsOnlineValidationO = RMSHandler.toRmsOnlineValidation(evtNo, equipmentNo, lotInfo.getLotId(), lotInfo.getDevice());
                    if (rmsOnlineValidationO == null) {
                        outTrx.setRtnCode(RMS_TIME_OUT);
                        outTrx.setRtnMesg("[EAP-RMS]:EAP 发送Device:[" + lotInfo.getDevice() + "]验证请求，RMS没有回复");
                        ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                        return;
                    }
                    if (!RMSResult.TRUE.equals(rmsOnlineValidationO.getResult())) {
                        outTrx.setRtnCode(RMS_FAILD);
                        outTrx.setRtnMesg("[EAP-RMS]:Device:[" + lotInfo.getDevice() + "]验证失败，原因:[" + rmsOnlineValidationO.getReason() + "]");
                        ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                        return;
                    }
                }
                ClientHandler.sendMessage(evtNo, false, 2, "[EAP-RMS]:Device:[" + lotInfo.getDevice() + "] RMS验证成功。");

                EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
                eapSingleParamCollectionI.setTrxId("EAPACCEPT");
                eapSingleParamCollectionI.setActionFlg("RPS");
                eapSingleParamCollectionI.setParameterName("");
                eapSingleParamCollectionI.setRequestKey(evtNo);
                String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
                if (StringUtils.isEmpty(returnMesg)) {
                    outTrx.setRtnCode(KVM_TIME_OUT);
                    outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 没有返回");
                    ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    return;
                }
                EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
                if (!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())) {
                    outTrx.setRtnCode(KVM_RETURN_ERROR);
                    outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
                    ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    return;
                }

                ClientHandler.sendMessage(evtNo, false, 2, "EAP发送Device 温度采集指令成功。");
            }
        }
    }

    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx Device 温度 采集完成
     */
    public void deviceTemperatureCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String lotId = lotInfo.getLotId();
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            Stateset("5","3",lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM采集Device 温度完成， 状态Error.");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        String eqptDeviceTemperature = inTrx.getOpeContent();
        JSONObject jsonObject = new JSONObject(eqptDeviceTemperature);
        String valueInJson = jsonObject.getString("");
        // 使用正则表达式提取数字部分
        String numericPart = valueInJson.replaceAll("[^\\d.]", "");
        ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device 温度完成。");
        String temperatureRang = lotInfo.getTemperatureRange();
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = lotInfo.getTemperature();
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if (value <= tem - tempRang || value >= tem + tempRang) {
            Stateset("5","3",lotId);
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "KVM Device 温度采集校验成功，设备Device 温度:[" + numericPart + "], 批次Device的温度:[" + temperature + "],在该温度范围内");
        str = lotInfo.getTemperature();
        Stateset("5","2",lotId);
        //PROBER机台做完操作后先进行check in，在给test机台下指令
        ClientHandler.sendMessage(evtNo, true, 2, "产前校验完成，请点击client端check in按钮开始check in");

    }

    private void testProgramVerification(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String lotId = lotInfo.getLotId();
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            Stateset("7","3",lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM 清除测试程式完成， 状态Error.");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        String eqpTestProgram = inTrx.getOpeContent();
        File file = new File(eqpTestProgram);
        String name = file.getName();
        int i = name.indexOf(".");
        String eapTestName = name.substring(0, i);

        String lotTestProgram = lotInfo.getTestProgram();
        if (!lotTestProgram.equals(eapTestName)) {
            Stateset("7","3",lotId);
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "] TestProgram:[" + lotTestProgram + "]与KVM采集的TestProgram:[" + eapTestName + "]不匹配，请确认");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, true, 2, "KVM测试程式验证通过，请点击prober机台start按钮开始作业");
        Stateset("7","2",lotId);
    }

    public static String getStr() {
        return str;
    }

    public static String getDn() {
        return dn;
    }

    public void Stateset(String step, String state,String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }
}
