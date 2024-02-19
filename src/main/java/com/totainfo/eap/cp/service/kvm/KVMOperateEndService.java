package com.totainfo.eap.cp.service.kvm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.totainfo.eap.cp.service.client.EAPLotIdReadService;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.ems.EMSGetAlarm.EMSGetAlarmO;
import com.totainfo.eap.cp.trx.ems.EMSGetAlarm.EMSGetAlarmOA;
import com.totainfo.eap.cp.trx.ems.EMSStatusReport.EMSStatusReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInI;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionO;
import com.totainfo.eap.cp.trx.kvm.KVMEliminatingAlerts.KVMEliminatingAlertsI;
import com.totainfo.eap.cp.trx.kvm.KVMEliminatingAlerts.KVMEliminatingAlertsO;
import com.totainfo.eap.cp.trx.kvm.KVMExitMode.KVMExitModeI;
import com.totainfo.eap.cp.trx.kvm.KVMExitMode.KVMExitModeO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.kvm.KVMSlotmapMode.KVMSlotmapModeI;
import com.totainfo.eap.cp.trx.kvm.KVMSlotmapMode.KVMSlotmapModeO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.*;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;


//import org.json.JSONObject;


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

    @Value("${ftp.host}")
    private String host;
    @Value("${ftp.port}")
    private int port;
    @Value("${ftp.user}")
    private String user;
    @Value("${ftp.password}")
    private String password;
    @Value("${ftp.path}")
    private String path;
    @Value("${equipment.testEqp}")
    private String testEqp;

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
            case "TIME":
                comperTime(evtNo, inTrx, outTrx);
                break;
            case "SLT":
                comperSlotMap(evtNo, inTrx, outTrx);
                break;
            default:      //KVM 远程操作指令结束
                operateEnd(evtNo, inTrx, outTrx);
                break;

        }
    }

    private void comperSlotMap(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String kvmSlotNo = inTrx.getOpeContent();
        if (kvmSlotNo.length() < 25) {
            ClientHandler.sendMessage(evtNo, false, 1, "kvm上传的slotmap错误");
            return;
        }
        LogUtils.info("KVM上报的slotmap信息为[" + kvmSlotNo + "]");
        LotInfo lotInfo = lotDao.getCurLotInfo();
        List<EAPReqLotInfoOB> paramList = lotInfo.getParamList();
        String datas = null;
        for (EAPReqLotInfoOB eapReqLotInfoOB : paramList) {
            if ("Sample".equals(eapReqLotInfoOB.getParamName())) {
                ObjectNode json2 = JacksonUtils.getJson2(eapReqLotInfoOB.getParamValue());
                JsonNode data = json2.get("datas");
                LogUtils.info("mes下发的wafer信息[" + data + "]");
                datas = data.toString();
                String[] indicesStr = datas.split(",");
                int[] positions = new int[indicesStr.length];
                for (int i = 0; i < indicesStr.length; i++) {
                    positions[i] = Integer.parseInt(indicesStr[i]);
                }
                char[] charArray = new char[25];
                for (int i = 0; i < charArray.length; i++) {
                    charArray[i] = '0'; // 初始设置为 '0'
                }
                for (int position : positions) {
                    if (position >= 1 && position <= 25) {
                        charArray[position - 1] = '1';
                    }
                }
                datas = new String(charArray);
            }
        }
        if (!datas.equals(kvmSlotNo)) {
            ClientHandler.sendMessage(evtNo, false, 1, "SlotMap校验失败,请检查");
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP  slotMap校验成功。");
        //PROBER机台做完操作后先进行check in，在给test机台下指令
        ClientHandler.sendMessage(evtNo, true, 2, "产前校验完成，请点击client端check in按钮开始check in");

    }

    private void comperTime(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String eqpTimeNow = inTrx.getOpeContent();
            LogUtils.info("机台上报时间[{}]", eqpTimeNow);
            LocalTime eqpTime = LocalTime.parse(eqpTimeNow, formatter);
            // 得到当前的北京时间
            ZonedDateTime beijingTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
            LocalTime localTimeNow = beijingTime.toLocalTime();
            LogUtils.info("北京时间是[{}]", localTimeNow);
            Duration duration = Duration.between(eqpTime, localTimeNow);
            long differenceInMinutes = duration.toMinutes();
            // 检查时间是否相差五分钟或以上
            if (differenceInMinutes >= 5 || differenceInMinutes <= -5) {
                ClientHandler.sendMessage(evtNo, true, 1, "机台时间[" + eqpTime + "]与北京时间[" + localTimeNow + "]相差五分钟以上，请检查");
                return;
            }
        } catch (Exception e) {
            ClientHandler.sendMessage(evtNo, false, 1, "KVM上传的时间格式错误");
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP机台校验时间通过。");

        //将信息下发给KVM
        EAPLotInfoWriteInI eapLotInfoWriteInI = new EAPLotInfoWriteInI();
        eapLotInfoWriteInI.setTrxId("EAPACCEPT");
        eapLotInfoWriteInI.setActionFlg("RJI");
        eapLotInfoWriteInI.setUserId(lotInfo.getUserId());
        eapLotInfoWriteInI.setProberCardId(EAPLotIdReadService.getProberCardId());
        eapLotInfoWriteInI.setLoadBoardId(lotInfo.getLoadBoardId());
        eapLotInfoWriteInI.setWaferLot(lotInfo.getWaferLot());
        eapLotInfoWriteInI.setDeviceId(lotInfo.getDeviceId());
        eapLotInfoWriteInI.setTestProgram(lotInfo.getTestProgram());
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapLotInfoWriteInI);
        Stateset("2", "1", lotInfo.getLotId());
        if (StringUtils.isEmpty(returnMesg)) {
            Stateset("2", "3", lotInfo.getLotId());
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM没有回复");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        EAPLotInfoWriteInO eapLotInfoWriteInO = JacksonUtils.string2Object(returnMesg, EAPLotInfoWriteInO.class);
        if (!RETURN_CODE_OK.equals(eapLotInfoWriteInO.getRtnCode())) {
            Stateset("2", "3", lotInfo.getLotId());
            outTrx.setRtnCode(eapLotInfoWriteInO.getRtnCode());
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM返回失败，原因:[" + eapLotInfoWriteInO.getRtnMesg() + "]");
            EapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
        }
        //发送给前端，LOT信息发送KVM成功
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:批次:[" + lotInfo.getLotId() + "]信息下发KVM成功。");
        MesHandler.eqptStatReport(evtNo, GenergicStatDef.EqptStat.RUN, "无", lotInfo.getUserId());
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
        //判断从GPIB获取的AlarmCode是否为空，为空不作处理；
        String alarmCode = GPIBHandler.getAlarmCode();
        if (StringUtils.isEmpty(alarmCode)) {
            return;
        }
        String time = DateUtils.getCurrentDateStr();
//        if("O0405".equals(alarmCode)){   //如实作业结束报警，直接结束
//            LotInfo lotInfo = lotDao.getCurLotInfo();
//            if(lotInfo == null){   //没有找到当前正在作业的批次信息
//                return;
//            }
//
//            //发送量测数据是否齐全的请求
//            EAPReqMeasureResultO eapReqMeasureResultO = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
//            String rtnCode = eapReqMeasureResultO.getRtnCode();
//            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]向mes请求量测数据");
//            int i = 0;
//            while (!RETURN_CODE_OK.equals(rtnCode) && i < max) {
//
//                try {
//                    TimeUnit.SECONDS.sleep(10);
//                } catch (InterruptedException e) {
//                    LogUtils.error("Sleep Exception", e);
//                }
//
//                EAPReqMeasureResultO Msg = MesHandler.measureResultReq(evtNo, lotInfo.getLotId());
//                rtnCode = Msg.getRtnCode();
//                i++;
//            }
//
//            if (!RETURN_CODE_OK.equals(rtnCode)) {
//                ClientHandler.sendMessage(evtNo, false, 2, "在轮询时间结束后，mes的量测数据仍未全部生成");
//                return;
//            }
//
//            ClientHandler.sendMessage(evtNo, false, 2, "[MES-EAP]量测结果均以生成");
//            EAPReqCheckOutO eapReqCheckOutO = MesHandler.checkOutReq(evtNo, lotInfo.getLotId());
//            if (!RETURN_CODE_OK.equals(eapReqCheckOutO.getRtnCode())) {
//                outTrx.setRtnCode(eapReqCheckOutO.getRtnCode());
//                outTrx.setRtnMesg(eapReqCheckOutO.getRtnMesg());
//                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
//                return;
//            }
//            ClientHandler.sendMessage(evtNo, true, 2, "[MES-EAP]批次:[" + lotInfo.getLotId() + "] Check Out 成功");
//
//            // 上传生产相关信息给ems
//            EMSLotInfoReportI emsLotInfoReportI = new EMSLotInfoReportI();
//            emsLotInfoReportI.setLotNo(lotInfo.getLotId());
//            emsLotInfoReportI.setDeviceName(lotInfo.getDevice());
//            emsLotInfoReportI.setEquipmentNo(GenericDataDef.equipmentNo);
//            emsLotInfoReportI.setTestProgram(lotInfo.getTestProgram());
//            emsLotInfoReportI.setProberCard(lotInfo.getProberCard());
//            emsLotInfoReportI.setProcessState("4");
//            emsLotInfoReportI.setOperator(lotInfo.getUserId());
//            emsLotInfoReportI.setTemperature(lotInfo.getTemperature());
//            ClientHandler.sendMessage(evtNo,false,2,"[EAP-EMS]:EAP给EMS上传结束生产相关信息指令成功");
//            EmsHandler.emsLotInfoReporToEms(evtNo,emsLotInfoReportI);
//
//            MesHandler.eqptStatReport(evtNo, EqptStat.IDLE,"无",lotInfo.getUserId());
//            remove(evtNo);
//            return;
//        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            lotInfo = new LotInfo();
            lotInfo.setLotId(_SPACE);
        }

        //如果报警已经存在，认为是重复上报，只更新是时间
        Map<String, AlarmInfo> alarmInfoMap = alarmDao.getAlarmInfo();
        if (alarmInfoMap.containsKey(alarmCode)) {
            AlarmInfo alarmInfo = alarmInfoMap.get(alarmCode);
            alarmInfo.setTime(time);
            alarmDao.addAlarmInfo(alarmInfo);
            return;
        }
//        ClientHandler.sendMessage(evtNo, true, 1, "[KVM-EAP]设备发送报警:[" + alarmCode + "]");
        //如果Alarm不存在，认为是新报警，将之前的报警清除
        AlarmInfo pvAlarmInfo;
        for (Map.Entry<String, AlarmInfo> entry : alarmInfoMap.entrySet()) {
            pvAlarmInfo = entry.getValue();
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备结束报警信息指令成功");
            MesHandler.eqptStatReport(evtNo, EqptStat.RUN, "无", lotInfo.getUserId());

            //给RCM上报报警结束时间
//            EapReportInfoI eapReportInfoI = new EapReportInfoI();
//            eapReportInfoI.setEquipmentState(EqptStat.RUN);
//            eapReportInfoI.setLotId(lotInfo.getLotId());
//            eapReportInfoI.setAlarmCode(pvAlarmInfo.getAlarmCode());
//            eapReportInfoI.setAlarmMessage(pvAlarmInfo.getAlarmText());
//            eapReportInfoI.setAlarmEndTime(pvAlarmInfo.getTime());
//            EapReportInfoO eapReportInfoO = RcmHandler.lotInfoReport(evtNo, eapReportInfoI);
//            if (!RETURN_CODE_OK.equals(eapReportInfoO.getRtnCode())) {
//                outTrx.setRtnCode(eapReportInfoO.getRtnCode());
//                outTrx.setRtnMesg("[EAP-RCM]:EAP上报批次信息，RCM返回失败，原因:[" + eapReportInfoO.getRtnMesg() + "]");
//                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
//                return;
//            }

            EmsHandler.alarmReportToEms(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(), lotInfo.getLotId(), "0");
            MesHandler.alarmReport(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(), pvAlarmInfo.getTime(), pvAlarmInfo.getId());
            alarmDao.removeAlarm(entry.getKey());
        }

        String alarmMessage = GPIBHandler.getAlarmMessage();

        String id = null;
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备开始报警信息指令成功");

        //给RCM上报报警开始时间
//        EapReportInfoI eapReportInfoI = new EapReportInfoI();
//        eapReportInfoI.setEquipmentState(EqptStat.DOWN);
//        eapReportInfoI.setLotId(lotInfo.getLotId());
//        eapReportInfoI.setAlarmCode(alarmCode);
//        eapReportInfoI.setAlarmMessage(alarmMessage);
//        eapReportInfoI.setAlarmBeginTime(time);
//        EapReportInfoO eapReportInfoO = RcmHandler.lotInfoReport(evtNo, eapReportInfoI);
//        if (!RETURN_CODE_OK.equals(eapReportInfoO.getRtnCode())) {
//            outTrx.setRtnCode(eapReportInfoO.getRtnCode());
//            outTrx.setRtnMesg("[EAP-RCM]:EAP上报批次信息，RCM返回失败，原因:[" + eapReportInfoO.getRtnMesg() + "]");
//            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
//            return;
//        }


        EmsHandler.alarmReportToEms(evtNo, alarmCode, alarmMessage, lotInfo.getLotId(), "1");
        EMSGetAlarmO alarmFromEms = EmsHandler.getAlarmFromEms(evtNo, alarmCode);
        List<EMSGetAlarmOA> itemList = alarmFromEms.getItemList();
        LogUtils.info("EMS返回报警定义管控的消息为[" + itemList + "]");
        for (EMSGetAlarmOA emsGetAlarmOA : itemList) {
            if ("Yes".equals(emsGetAlarmOA.getNeedClear())) {
                KVMEliminatingAlertsI kvmEliminatingAlertsI = new KVMEliminatingAlertsI();
                kvmEliminatingAlertsI.setTrxId("EAPACCEPT");
                kvmEliminatingAlertsI.setActionFlg("ELA");
                kvmEliminatingAlertsI.setEapId(equipmentNo);
                String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmEliminatingAlertsI);
                if (StringUtils.isEmpty(returnMesg)) {
                    outTrx.setRtnCode(KVM_TIME_OUT);
                    outTrx.setRtnMesg("[EAP-KVM]:EAP下发自动消警信息，KVM没有回复");
                    ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                    return;
                }
                KVMEliminatingAlertsO kvmEliminatingAlertsO = JacksonUtils.string2Object(returnMesg, KVMEliminatingAlertsO.class);
                if (!RETURN_CODE_OK.equals(kvmEliminatingAlertsO.getRtnCode())) {
                    outTrx.setRtnCode(kvmEliminatingAlertsO.getRtnCode());
                    outTrx.setRtnMesg("[EAP-KVM]:EAP下发自动消警信息，KVM返回失败，原因:[" + kvmEliminatingAlertsO.getRtnMesg() + "]");
                    ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                    return;
                }
            }
            if ("Yes".equals(emsGetAlarmOA.getNeedRefund())) {
                KVMExitModeI kvmExitModeI = new KVMExitModeI();
                kvmExitModeI.setTrxId("EAPACCEPT");
                kvmExitModeI.setActionFlg("REX");
                kvmExitModeI.setEqpId(equipmentNo);
                String returnMesage = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmExitModeI);
                if (StringUtils.isEmpty(returnMesage)) {
                    outTrx.setRtnCode(KVM_TIME_OUT);
                    outTrx.setRtnMesg("[EAP-KVM]:EAP下发退片信息，KVM没有回复");
                    ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                    return;
                }
                KVMExitModeO kvmExitModeO = JacksonUtils.string2Object(returnMesage, KVMExitModeO.class);
                if (!RETURN_CODE_OK.equals(kvmExitModeO.getRtnCode())) {
                    outTrx.setRtnCode(kvmExitModeO.getRtnCode());
                    outTrx.setRtnMesg("[EAP-KVM]:EAP下发退片信息，KVM返回失败，原因:[" + kvmExitModeO.getRtnMesg() + "]");
                    ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                    return;
                }
            }
        }


        EAPEqptAlarmReportO eapEqptAlarmReportO = MesHandler.alarmReport(evtNo, alarmCode, alarmMessage, time, id);
        if (eapEqptAlarmReportO != null) {
            id = eapEqptAlarmReportO.getRtnMesg();
            LogUtils.info("mes返回的id是[{}]", id);
        }
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmCode(alarmCode);
        alarmInfo.setAlarmText(alarmMessage);
        alarmInfo.setTime(time);
        alarmInfo.setId(id);
        alarmDao.addAlarmInfo(alarmInfo);
        MesHandler.eqptStatReport(evtNo, EqptStat.DOWN, "无", lotInfo.getUserId());
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
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备状态信息指令成功");
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

    private void operateEnd(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
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

        //eap给kvm下发指令执行slotmap代操
        KVMSlotmapModeI kvmSlotmapModeI = new KVMSlotmapModeI();
        kvmSlotmapModeI.setTrxId("EAPACCEPT");
        kvmSlotmapModeI.setActionFlg("SLT");
        kvmSlotmapModeI.setEqpId(equipmentNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmSlotmapModeI);
        if (StringUtils.isEmpty(returnMesg)) {
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发slotmap代操指令， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        KVMSlotmapModeO kvmSlotmapModeO = JacksonUtils.string2Object(returnMesg, KVMSlotmapModeO.class);
        if (!RETURN_CODE_OK.equals(kvmSlotmapModeO.getRtnCode())) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发slotmap代操指令， KVM 返回错误:[" + kvmSlotmapModeO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送slotMap采集指令成功。");
//        //PROBER机台做完操作后先进行check in，在给test机台下指令
//        ClientHandler.sendMessage(evtNo, true, 2, "产前校验完成，请点击client端check in按钮开始check in");

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

        Path dataPath = Paths.get(System.getProperty("user.dir"), "data", "MES_DATA" + ".TXT");
        if (!Files.exists(dataPath.getParent())) {
            try {
                Files.createDirectories(dataPath.getParent());
            } catch (IOException e) {
                LogUtils.error("文件夹创建失败");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("TP_NAME:").append(lotInfo.getTestProgram()).append("\n")
                .append("TESTER_ID:").append(testEqp).append("\n")
                .append("PART_NO:").append("NA").append("\n")
                .append("QTY:").append(lotInfo.getParamMap().get("Sample").length()).append("\n")
                .append("C_LOT:").append("NA").append("\n")
                .append("HANDLER:").append("NA").append("\n")
                .append("PROBER:").append("NA").append("\n")
                .append("LOADBOARD:").append("NA").append("\n")
                .append("PROBERCARD:").append(lotInfo.getProberCard()).append("\n")
                .append("SOCKET:").append("NA").append("\n")
                .append("PROCESS:").append("CP").append("\n")
                .append("STEP:").append("NA").append("\n")
                .append("RETEST:").append("NA").append("\n")
                .append("RT_BIN:").append("NA").append("\n")
                .append("SUBCON_NAME:").append("NA").append("\n")
                .append("SUBCON_LOT:").append("NA").append("\n")
                .append("DATE_CODE:").append("NA").append("\n")
                .append("OPID:").append(lotInfo.getUserId()).append("\n")
                .append("PASS_BIN:").append("NA").append("\n")
                .append("WORK_MODE:").append("TEST").append("\n")
                .append("EXTENSION:").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"))).append("\n");
        try {
            Files.write(dataPath, sb.toString().getBytes());
            FtpUtils.uploadFile(host, user, password, port, path, dataPath.toString());
            Files.delete(dataPath);
        } catch (IOException e) {
            LogUtils.error("xtr文件写入失败");
        }
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

    public void EapEndCard(String evtNo) {
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
    }

    public void remove(String evtNo) {
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

    public EapReportInfoO eapReportInfoO(String evtNo, EapReportInfoI eapReportInfoI) {
        EapReportInfoO eapReportInfoO = new EapReportInfoO();
        eapReportInfoI.setTrxId("eapReportAlarmInfo");
        eapReportInfoI.setEquipmentNo(equipmentNo);
        String returnMsgFromRcm = httpHandler.postHttpForRcm(evtNo, GenericDataDef.rcmUrl, eapReportInfoI);
        if (!org.springframework.util.StringUtils.hasText(returnMsgFromRcm)) {
            eapReportInfoO.setRtnCode("00000001");
            eapReportInfoO.setRtnMesg("[EAP-RCM]:EAP上报设备信息，RCM没有回复");
            ClientHandler.sendMessage(evtNo, false, 1, eapReportInfoO.getRtnMesg());
        } else {
            eapReportInfoO = JacksonUtils.string2Object(returnMsgFromRcm, EapReportInfoO.class);
        }
        return eapReportInfoO;
    }

}

