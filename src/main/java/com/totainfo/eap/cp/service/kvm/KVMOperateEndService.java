package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptStat;
import com.totainfo.eap.cp.commdef.GenergicStatDef.KVMOperateState;
import com.totainfo.eap.cp.commdef.GenergicStatDef.RMSResult;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IAlarmDao;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.ems.EMSStatusReport.EMSStatusReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultO;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.backoff.Sleeper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.event.TreeWillExpandListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private IEqptDao eqptDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private IAlarmDao alarmDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.rms.checkFlag}")
    private boolean rmsCheckFlag;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Value("${number.max}")
    private int max;

    private Object lock = new Object();

    private static String str;

    private static String dn;

    private String stop;

    @Override
    public void mainProc(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String actionFlg = inTrx.getActionFlg();
        switch (actionFlg) {
            case "SCM":    //KVM和eap同步连线状态
                syncConnectMode(evtNo, inTrx, outTrx);
                break;
            case "RRTU":   //  KVM报警信息上报
                alarmReport(evtNo, inTrx, outTrx);
                break;
            case "REE":   //KVM状态上报
                eqptStatReport(evtNo, inTrx, outTrx);
                break;
            default:      //KVM 远程操作指令结束
                operateEnd(evtNo, inTrx, outTrx);
                break;

        }
    }

    private void syncConnectMode(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        LotInfo curLotInfo = lotDao.getCurLotInfo();
        if (curLotInfo == null) {
            curLotInfo = new LotInfo();
            curLotInfo.setUserId("null");
            curLotInfo.setLotId("null");
            curLotInfo.setProberCard("null");
        }

        EqptInfo eqptInfo = eqptDao.getEqpt();
        if (eqptInfo == null) {
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptStat(EqptStat.IDLE);
            eqptInfo.setEqptMode(EqptMode.Online);
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
    }

    private void alarmReport(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        //KVM报警信息上报
        String alarmCode = inTrx.getAlarmCode();
        String time = DateUtils.getCurrentDateStr();
        ClientHandler.sendMessage(evtNo, true, 1, "[KVM-EAP]设备发送报警:[" + alarmCode + "]");
        if("O405".equals(alarmCode)){   //如实作业结束报警，直接结束
            LotInfo lotInfo = lotDao.getCurLotInfo();
            if(lotInfo == null){   //没有找到当前正在作业的批次信息
                return;
            }

            //发送量测数据是否齐全的请求
            EAPReqMeasureResultO eapReqMeasureResultO = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
            String rtnCode = eapReqMeasureResultO.getRtnCode();
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]向mes请求量测数据");
            int i = 0;
            while (!RETURN_CODE_OK.equals(rtnCode) && i < max) {

                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    LogUtils.error("Sleep Exception", e);
                }

                EAPReqMeasureResultO Msg = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
                rtnCode = Msg.getRtnCode();
                i++;
            }

            if (!RETURN_CODE_OK.equals(rtnCode)) {
                ClientHandler.sendMessage(evtNo, false, 2, "在轮询时间结束后，mes的量测数据仍未全部生成");
                return;
            }

            ClientHandler.sendMessage(evtNo, false, 2, "[MES-EAP]量测结果均以生成");
            EAPReqCheckOutO eapReqCheckOutO = MesHandler.checkOutReq(evtNo, lotInfo.getLotId());
            if (!RETURN_CODE_OK.equals(eapReqCheckOutO.getRtnCode())) {
                outTrx.setRtnCode(eapReqCheckOutO.getRtnCode());
                outTrx.setRtnMesg(eapReqCheckOutO.getRtnMesg());
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, true, 2, "[MES-EAP]批次:[" + lotInfo.getLotId() + "] Check Out 成功");
            remove(evtNo);
            return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            lotInfo = new LotInfo();
            lotInfo.setLotId(_SPACE);
        }
        //如果报警已经存在，认为是重复上报，只更新是时间
        Map<String, AlarmInfo> alarmInfoMap = alarmDao.getAlarmInfo();
        if(alarmInfoMap.containsKey(alarmCode)){
            AlarmInfo alarmInfo = alarmInfoMap.get(alarmCode);
            alarmInfo.setTime(time);
            alarmDao.addAlarmInfo(alarmInfo);
            return;
        }
        //如果Alarm不存在，认为是新报警，将之前的报警清除
        AlarmInfo pvAlarmInfo;
        for(Map.Entry<String,AlarmInfo> entry:alarmInfoMap.entrySet()){
            pvAlarmInfo = entry.getValue();
            EmsHandler.alarmReportToEms(evtNo,pvAlarmInfo.getAlarmCode(),pvAlarmInfo.getAlarmText(),lotInfo.getLotId(),"0");
            MesHandler.alarmReport(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(),pvAlarmInfo.getTime(), pvAlarmInfo.getID()) ;
            alarmDao.removeAlarm(entry.getKey());
        }

        //判断从GPIB获取的AlarmCode是否为空，为空不作处理；
        String realCode = GPIBHandler.getAlarmCode();
        if(StringUtils.isEmpty(realCode) ){
            return;
        }
        String alarmMessage = GPIBHandler.getAlarmMessage();

        String id= null;
        EmsHandler.alarmReportToEms(evtNo, alarmCode, alarmMessage, lotInfo.getLotId(), "1");
        EAPEqptAlarmReportO eapEqptAlarmReportO = MesHandler.alarmReport(evtNo, alarmCode, alarmMessage, time,null);
        if(eapEqptAlarmReportO != null){
            id = eapEqptAlarmReportO.getID();
        }
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmCode(alarmCode);
        alarmInfo.setAlarmText(alarmMessage);
        alarmInfo.setTime(time);
        alarmInfo.setID(id);
        alarmDao.addAlarmInfo(alarmInfo);
        ClientHandler.sendMessage(evtNo, true, 1, "[KVM-EAP]设备发送报警:[" + alarmCode + "][" + alarmMessage + "]");
    }

    private void eqptStatReport(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String eqptStat = inTrx.getState();
        String lastState = null;
        EqptInfo eqptInfo = eqptDao.getEqptWithLock();
        if (eqptInfo == null) {
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(GenericDataDef.equipmentNo);
            eqptInfo.setEqptMode(EqptMode.Online);
        }
        //状态没变不做处理
        if (eqptStat.equals(eqptInfo.getEqptStat())) {
            ClientHandler.sendMessage(evtNo, false, 2, "[Client-EAP]设备状态未发生变化");
            return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (eqptStat.equals(GenergicStatDef.EqptStat.RUN)) {
            if (lotInfo != null) {
                ClientHandler.sendMessage(evtNo, false, 2, "[Client-Eap]KVM上报设备状态切换为Run");
                lastState = "1";
            } else {
                ClientHandler.sendMessage(evtNo, false, 1, "未找到相应的制程信息");
                return;
            }
        } else {
            if (lotInfo != null) {
                eqptStat = GenergicStatDef.EqptStat.DOWN;
                lastState = "3";
                ClientHandler.sendMessage(evtNo, false, 2, "[Client-Eap]KVM上报设备状态切换为Down");
            } else {
                eqptStat = GenergicStatDef.EqptStat.IDLE;
                lastState = "2";
                ClientHandler.sendMessage(evtNo, false, 1, "[Client-Eap]KVM上报设备状态切换为Idle");
            }
        }

        EMSStatusReportO emsStatusReportO = EmsHandler.emsStatusReportToEms(evtNo, eqptInfo.getEqptMode(), lastState, eqptStat);
        if (!RETURN_CODE_OK.equals(emsStatusReportO.getRtnCode())) {
            outTrx.setRtnCode(emsStatusReportO.getRtnCode());
            outTrx.setRtnMesg(emsStatusReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
        }
        eqptInfo.setEqptStat(eqptStat);
        eqptDao.addEqpt(eqptInfo);


        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("ConnectInfo");
        eapSyncEqpInfoI.setTrypeId("I");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());

        if (lotInfo != null) {
            eapSyncEqpInfoI.setUserId(lotInfo.getUserId());
            eapSyncEqpInfoI.setLotNo(lotInfo.getLotId());
            eapSyncEqpInfoI.setProberCardId(lotInfo.getProberCard());
        }
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
    }

    private void operateEnd(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx){
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
                break;
            case "G":   // 实时检验温度
                realTimeTemperatureCollection(evtNo, inTrx, outTrx);
                break;
        }
    }

    private void realTimeTemperatureCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            EapEndCard(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM采集Device 实时温度完成， 状态Error.");
            EapEndCard(evtNo);
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        String eqptDeviceTemperature = inTrx.getOpeContent();
        // 使用正则表达式提取数字部分
        String numericPart = eqptDeviceTemperature.replaceAll("[^\\d.]", "");

        String temperatureRang = lotInfo.getTemperatureRange();
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = lotInfo.getTemperature();
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if (value <= tem - tempRang || value >= tem + tempRang) {
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            EapEndCard(evtNo);
            ClientHandler.sendMessage(evtNo, true, 1, outTrx.getRtnMesg());
            return;
        }
    }


    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx 下发采集Device Name，并与Lot Device Name进行比对
     */
    private void sendDeviceCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String lotId = lotInfo.getLotId();
        Stateset("2", "2", lotId);
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM 批次信息写入成功.");
        GPIBHandler.getDeviceName();
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-GPIB]:EAP发送Device name采集指令成功。");
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
            EapEndCard(evtNo);
            remove(evtNo);
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
                if (eapCheckName) {
                    String lotDeviceName = lotInfo.getDevice();
                    if (!lotDeviceName.equals(eqptDeviceName)) {
                        Stateset("3", "3", lotId);
                        outTrx.setRtnCode(DEVICE_DISMATCH);
                        outTrx.setRtnMesg("[EAP-KVM]:批次:[" + lotInfo.getLotId() + "]校验失败Device:[" + lotDeviceName + "]，KVM采集的Device:[" + eqptDeviceName + "]，请确认");
                        EapEndCard(evtNo);
                        remove(evtNo);
                        ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                        return;
                    }
                    ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM Device Name采集校验成功，设备Device Name:[" + eqptDeviceName + "], 批次Device:[" + lotDeviceName + "]");
                }
                Stateset("3", "2", lotId);
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
            Stateset("4", "1", lotId);
            if (StringUtils.isEmpty(returnMesg)) {
                Stateset("4", "3", lotId);
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 没有返回");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
            if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
                Stateset("4", "3", lotId);
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
                EapEndCard(evtNo);
                remove(evtNo);
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
            EapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }

        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM 代操完成。");
        String lotId = lotInfo.getLotId();
        Stateset("4", "2", lotId);
        if (rmsCheckFlag) {
            LogUtils.info("开始发给rms做校验请求");
            Stateset("5", "1", lotId);
            RmsOnlineValidationO rmsOnlineValidationO = RMSHandler.toRmsOnlineValidation(evtNo, equipmentNo, lotInfo.getLotId(), lotInfo.getDevice());
            if (rmsOnlineValidationO == null) {
                Stateset("5", "3", lotId);
                outTrx.setRtnCode(RMS_TIME_OUT);
                outTrx.setRtnMesg("[EAP-RMS]:EAP 发送Device:[" + lotInfo.getDevice() + "]验证请求，RMS没有回复");
                EapEndCard(evtNo);
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            if (!RMSResult.TRUE.equals(rmsOnlineValidationO.getResult())) {
                Stateset("5", "3", lotId);
                outTrx.setRtnCode(RMS_FAILD);
                outTrx.setRtnMesg("[EAP-RMS]:Device:[" + lotInfo.getDevice() + "]验证失败，原因:[" + rmsOnlineValidationO.getReason() + "]");
                EapEndCard(evtNo);
                remove(evtNo);
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-RMS]:Device:[" + lotInfo.getDevice() + "] RMS验证成功。");
            Stateset("5", "2", lotId);
        }
        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
        eapSingleParamCollectionI.setActionFlg("RPS");
        eapSingleParamCollectionI.setParameterName("");
        eapSingleParamCollectionI.setRequestKey(evtNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
        Stateset("6", "1", lotId);
        if (StringUtils.isEmpty(returnMesg)) {
            Stateset("6", "3", lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
        if (!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())) {
            Stateset("6", "3", lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
            EapEndCard(evtNo);
            remove(evtNo);
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
            Stateset("6", "3", lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM采集Device 温度完成， 状态Error.");
            EapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        String eqptDeviceTemperature = inTrx.getOpeContent();
        // 使用正则表达式提取数字部分
        String numericPart = eqptDeviceTemperature.replaceAll("[^\\d.]", "");
        ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device 温度完成。");
        String temperatureRang = lotInfo.getTemperatureRange();
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = lotInfo.getTemperature();
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if (value <= tem - tempRang || value >= tem + tempRang) {
            Stateset("6", "3", lotId);
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            EapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "KVM Device 温度采集校验成功，设备Device 温度:[" + numericPart + "], 批次Device的温度:[" + temperature + "],在该温度范围内");
        str = lotInfo.getTemperature();
        Stateset("6", "2", lotId);
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
            Stateset("8", "3", lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("KVM 清除测试程式完成， 状态Error.");
            EapEndCard(evtNo);
            remove(evtNo);
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
            Stateset("8", "3", lotId);
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "] TestProgram:[" + lotTestProgram + "]与KVM采集的TestProgram:[" + eapTestName + "]不匹配，请确认");
            EapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, true, 2, "KVM测试程式验证通过，请点击prober机台start按钮开始作业");
        Stateset("8", "2", lotId);
    }

    public static String getStr() {
        return str;
    }

    public static String getDn() {
        return dn;
    }

    public void Stateset(String step, String state, String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }
    public void EapEndCard(String evtNo){
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
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
