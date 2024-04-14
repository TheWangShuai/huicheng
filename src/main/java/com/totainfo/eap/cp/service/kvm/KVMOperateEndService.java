package com.totainfo.eap.cp.service.kvm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenergicStatDef.*;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IAlarmDao;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.*;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.mode.ValidationItem;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.client.EAPValidation.EAPValidationI;
import com.totainfo.eap.cp.trx.client.EAPValidation.EAPValidationIA;
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
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportI;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportO;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyI;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.*;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;


//import org.json.JSONObject;


/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 9:31
 */
@SuppressWarnings("ALL")
@Service("EAPACCEPT")
public class KVMOperateEndService extends EapBaseService<KVMOperateEndI, KVMOperateEndO> {

    @Value("${equipment.id}")
    private String proberName;

    @Value("${equipment.tsId}")
    private String tsId;

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
    @Resource
    private ClientHandler clientHandler;
    @Resource
    private KvmHandler kvmHandler;
    @Value("${spring.rabbitmq.rms.checkFlag}")
    private boolean rmsCheckFlag;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Value("${time.max}")
    private int timeMax;

    @Value("${spring.rabbitmq.ems.checkFlag}")
    private boolean emsCheckFlag;

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
    @Value("${ftp.waferIdPath}")
    private String waferIdPath;
    @Value("${equipment.testerId}")
    private String testerId;
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
            case "RTC":
                comperTestProgram(evtNo, inTrx, outTrx);
                break;
            default:      //KVM 远程操作指令结束
                operateEnd(evtNo, inTrx, outTrx);
                break;

        }
    }

    private void comperTestProgram(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        LogUtils.info("EAP下发清除FunctionKey指令，Prober机台返回结果为：：[" + inTrx.getOpeContent()  + "]");
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }
        String testProgram = inTrx.getOpeContent();

        if (StringUtils.isEmpty(testProgram)) {
            outTrx.setRtnCode(TESTER_PROGRAM_ERROR);
            outTrx.setRtnMesg("[EAP-Client]: KVM返回的程式为空！！！");
            return;
        }
        // KVM下发程式格式为X:\XXX\XXXXXXX\XXXXXXX.tdl
        String[] split = testProgram.split("\\\\");
        String probeProgram = split[2];
        if (!probeProgram.contains(lotInfo.getTestProgram())){
            outTrx.setRtnCode(TESTER_PROGRAM_ERROR);
            outTrx.setRtnMesg("[EAP-Client]: KVM返回的程式为：[" + inTrx.getOpeContent() + "], MES中返回的程式为：[" + lotInfo.getTestProgram() + "], 请确认！");
            KvmHandler.haltStop(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
        }
    }

    private void comperSlotMap(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        String kvmSlotNo = inTrx.getOpeContent();
        if (kvmSlotNo.length() < 25) {
            outTrx.setRtnCode(SLOT_MAP_ERROR);
            outTrx.setRtnMesg("kvm上传的slotmap错误,SlotMap:[" + kvmSlotNo + "]");
            return;
        }
        LogUtils.info("KVM上报的slotmap信息为[" + kvmSlotNo + "]");
        LotInfo lotInfo = lotDao.getCurLotInfo();
        List<EAPReqLotInfoOB> paramList = lotInfo.getParamList();
        String datas = null;
        String data;
        String[] indicesStr;
        Optional<EAPReqLotInfoOB> optional =  paramList.stream().filter(o->"Sample".equals(o.getParamName())).findFirst();
        if(!optional.isPresent()){
            outTrx.setRtnCode(SLOT_MAP_ERROR);
            outTrx.setRtnMesg("MES Lot信息中没有SlotMap（Sample）参数，请检查");
            return;
        }

        EAPReqLotInfoOB eapReqLotInfoOB = optional.get();
        ObjectNode objectNode = JacksonUtils.getJson2(eapReqLotInfoOB.getParamValue());
        String sampleSlot = objectNode.get("datas").asText();
        LogUtils.info("mes下发的wafer信息[" + sampleSlot + "]");
        String[] slots = sampleSlot.split(",");
        int index;
        String flag;
        for(String slot: slots){
            index = Integer.parseInt(slot);
            flag = kvmSlotNo.substring(index-1, index);
            if(!"1".equals(flag)){
                outTrx.setRtnCode(SLOT_MAP_ERROR);
                outTrx.setRtnMesg("Slot:[" + slot + "]需要抽检，但是KVM上报对应SLot没有Wafer，请检查");
                return;
            }
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP  slotMap校验成功。");

        // 发送校验给client
//        List<EAPValidationIA> eapValidationIAList = new ArrayList<>();
//        List<ValidationItem> validationItemList = ValidationInfo.getValidationItemList();
//        if (!CollectionUtils.isEmpty(validationItemList)){
//            for (ValidationItem validationItem : validationItemList) {
//                String paramName = validationItem.getParamName();
//                List<EAPReqLotInfoOB> paramList4Mes = lotInfo.getParamList1();
//                Map<String, String> paramMap4Mes = paramList4Mes.stream().collect(Collectors.toMap(EAPReqLotInfoOB::getParamName, EAPReqLotInfoOB::getParamValue, (k1, k2) -> k1));
//                boolean mesParamFlag = paramMap4Mes.containsKey(paramName);
//                if (mesParamFlag){
//                    String paramValue4Mes = paramMap4Mes.get(paramName);
//                    validationItem.setDefaultValue(paramValue4Mes);
//                }
//                String defaultValue = validationItem.getDefaultValue();
//                String actualValue = validationItem.getActualValue();
//                String paramId = validationItem.getParamId();
//                String remark = validationItem.getRemark();
//
//                EAPValidationIA eapValidationIA = new EAPValidationIA();
//                String content;
//                if (defaultValue.equals(actualValue)){
//                    eapValidationIA.setIsUpdates(false);
//                    if (!mesParamFlag){
//                        content = String.format("重要参数[%s]机台设置的值为[%s],默认设置为[%s],已调整为[%s]",paramName,actualValue,defaultValue,defaultValue);
//                    }else {
//                        content = String.format("重要参数[%s]机台设置的值为[%s],MES设置为[%s],已调整为[%s]",paramName,actualValue,defaultValue,defaultValue);
//                    }
//                }else {
//                    eapValidationIA.setIsUpdates(true);
//                    if (!mesParamFlag){
//                        content = String.format("重要参数[%s]机台设置的值为[%s],默认设置为[%s],参数校验一致",paramName,actualValue,defaultValue);
//                    }else {
//                        content = String.format("重要参数[%s]机台设置的值为[%s],MES设置为[%s],参数校验一致",paramName,actualValue,defaultValue);
//                    }
//                }
//                eapValidationIA.setId(validationItem.getParamNo());
//                eapValidationIA.setContent(content);
//                eapValidationIAList.add(eapValidationIA);
//            }
//        }else {
//            //todo abnormal flow
//
//        }
//        //todo send to client
//        EAPValidationI eapValidationI = new EAPValidationI();
//        eapValidationI.setInfos(eapValidationIAList);
//        eapValidationI.setActionFlg("");
//        eapValidationI.setTrxId("");


        //第八步SlotMap校验结束
        clientHandler.setFlowStep(StepName.EIGTH,StepStat.COMP);
        //PROBER机台做完操作后先进行check in，在给test机台下指令
        ClientHandler.sendMessage(evtNo, true, 2, "产前校验完成，请点击client端check in按钮开始check in");
        EmsHandler.reportRunWorkInfo(evtNo,"SlotMap校验完成",lotInfo.getLotId(),"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
        LogUtils.info("Operation Complete");
    }

    private void comperTime(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        int counnt = 0;
        LotInfo lotInfo = lotDao.getCurLotInfo();
        LocalDateTime eqpTime = null;
        if(lotInfo == null){
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }
        String eqpTimeNow = inTrx.getOpeContent();
        //第二步时间检验开始
        clientHandler.setFlowStep(StepName.SECOND,StepStat.INPROCESS);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LogUtils.info("机台上报时间[{}]", eqpTimeNow);
            eqpTime = LocalDateTime.parse(eqpTimeNow, formatter);
            // 得到当前的北京时间
            ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
            LocalDateTime localDateTime = LocalDateTime.now(beijingZone);
            String formattedDateTime = localDateTime.format(formatter);
            LocalDateTime bjNowTime = LocalDateTime.parse(formattedDateTime, formatter);
            LogUtils.info("北京时间是[{}]", bjNowTime);
            // 检查时间是否相差五分钟或以上
            Duration duration = Duration.between(eqpTime, bjNowTime);
            long differenceInMinutes = duration.toMinutes();
            if (differenceInMinutes >= 5 || differenceInMinutes <= -5) {
                outTrx.setRtnCode("0000001");
                outTrx.setRtnMesg("机台时间[" + eqpTime + "]与北京时间[" + bjNowTime + "]相差五分钟以上，请检查");
                return;
            }
        } catch (Exception e) {
            ClientHandler.sendMessage(evtNo, false, 2, "EAP机台校验时间失败。机台时间为: " + eqpTime + "],北京时间为: [" + eqpTimeNow);
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP机台校验时间通过。");
        EmsHandler.reportRunWorkInfo(evtNo,"时间校验结束",lotInfo.getLotId(),"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());

        //第二步时间检验结束
        clientHandler.setFlowStep(StepName.SECOND,StepStat.COMP);
        // EAP下发采集Device Name指令
        GPIBHandler.getDeviceName();
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-GPIB]:EAP发送Device name采集指令成功。");


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
        EAPSyncEqpInfoI eapSyncEqpInfoI = getEapSyncEqpInfoI(curLotInfo, eqptInfo);
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        outTrx.setTrxId("KVMSyncConnectionMode");
        outTrx.setModel(eqptInfo.getEqptMode());
        outTrx.setRtnMesg(RETURN_MESG_OK);
    }

    private static EAPSyncEqpInfoI getEapSyncEqpInfoI(LotInfo curLotInfo, EqptInfo eqptInfo) {
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setUserId(curLotInfo.getUserId());
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        eapSyncEqpInfoI.setLotNo(curLotInfo.getLotId());
        eapSyncEqpInfoI.setProbeCardId(curLotInfo.getProberCard());
        eapSyncEqpInfoI.setFoupLotNo(curLotInfo.getLotId());
        eapSyncEqpInfoI.setGpibState("0");
        return eapSyncEqpInfoI;
    }

    private void alarmReport(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {

        //KVM报警信息上报
        //判断从GPIB获取的AlarmCode是否为空，为空不作处理；
        String alarmCode = inTrx.getAlarmCode();
        String path = inTrx.getPath();
        String alarmMessage = null;
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String cleanFlg = "N";
        if (StringUtils.isEmpty(alarmCode)) {
            return;
        }
        if (StringUtils.isEmpty(path)) {
            return;
        }
        EMSGetAlarmO alarmFromEms = EmsHandler.getAlarmFromEms(evtNo, alarmCode);
        List<EMSGetAlarmOA> itemList = alarmFromEms.getItemList();
        for (EMSGetAlarmOA emsGetAlarmOA : itemList) {
            if (alarmCode.equals(emsGetAlarmOA.getAlarmCode())) {
                alarmMessage = emsGetAlarmOA.getAlarmText();
            }
        }
        String time = DateUtils.getCurrentDateStr();

        if (lotInfo == null) {
            lotInfo = new LotInfo();
            lotInfo.setLotId(_SPACE);
        }

        //如果报警已经存在，认为是重复上报，只更新是时间
        AlarmInfo alarmInfo;
        Map<String, AlarmInfo> alarmInfoMap = alarmDao.getAlarmInfo();
        if (alarmInfoMap.containsKey(alarmCode)) {
            alarmInfo = alarmInfoMap.get(alarmCode);
            alarmInfo.setAlarmEndTime(time);
            alarmDao.addAlarmInfo(alarmInfo);
            return;
        }
        //如果Alarm不存在，认为是新报警，将之前的报警清除
        AlarmInfo pvAlarmInfo;
        for (Map.Entry<String, AlarmInfo> entry : alarmInfoMap.entrySet()) {
            pvAlarmInfo = entry.getValue();
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备结束报警信息指令成功");
            MesHandler.eqptStatReport(evtNo, EqptStat.RUN, "无", lotInfo.getUserId());

            EmsHandler.alarmReportToEms(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(), lotInfo.getLotId(), "1", inTrx.getPath());
            MesHandler.alarmReport(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(), time, pvAlarmInfo.getId());
            alarmDao.removeAlarm(entry.getKey());
        }
        String id = null;
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备开始报警信息指令成功");

        LogUtils.info("获取到自动消除报警的flg为： [" + emsCheckFlag + "]");
        if (emsCheckFlag) {
            LogUtils.info("自动消除报警开始!!");
            EmsHandler.alarmReportToEms(evtNo, alarmCode, alarmMessage, lotInfo.getLotId(), "0", inTrx.getPath());
            alarmFromEms = EmsHandler.getAlarmFromEms(evtNo, alarmCode);
            itemList = alarmFromEms.getItemList();
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
        }


        EAPEqptAlarmReportO eapEqptAlarmReportO = MesHandler.alarmReport(evtNo, alarmCode, alarmMessage, time, id);
        if (eapEqptAlarmReportO != null) {
            id = eapEqptAlarmReportO.getRtnMesg();
            LogUtils.info("mes返回的id是[{}]", id);
        }
        alarmInfo = new AlarmInfo();
        alarmInfo.setAlarmCode(alarmCode);
        alarmInfo.setAlarmText(alarmMessage);
        alarmInfo.setTime(time);
        alarmInfo.setId(id);
        alarmInfo.setAlarmImg(path);
        alarmDao.addAlarmInfo(alarmInfo);
        MesHandler.eqptStatReport(evtNo, EqptStat.DOWN, "无", lotInfo.getUserId());
        ClientHandler.sendMessage(evtNo, false, 1, "[KVM-EAP]设备发送报警:[" + alarmCode + "][" + alarmMessage + "]");
        if(lotInfo == null){
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }

        // 判断是否为CheckIn后报警
        if (StringUtils.isNotEmpty(stateDao.getStateInfo().getStep()) && Integer.parseInt(stateDao.getStateInfo().getStep()) > 7){
            LogUtils.info("流程当前步骤为：：[" + stateDao.getStateInfo().getStep()  + "]");
            cleanFlg = "Y";
            CleanFuncKeyO cleanFuncKeyO = KvmHandler.cleanFuncKey(evtNo, cleanFlg);
            if ("0000000".equals(cleanFuncKeyO.getRtnCode())) {
                ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]: EAP 下发清除FunctionKey成功！ ");
            }
        }
    }

    private void eqptStatReport(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String eqptStat = inTrx.getState();
        String lastState = null;
        EqptInfo eqptInfo = eqptDao.getEqptWithLock();
        if (eqptInfo == null) {
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(equipmentNo);
            eqptInfo.setEqptMode(EqptMode.Online);
        }
        //状态没变不做处理
        if (eqptStat.equals(eqptInfo.getEqptStat())) {
            ClientHandler.sendMessage(evtNo, false, 2, "[Client-EAP]设备状态未发生变化");
            return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (eqptStat.equals(EqptStat.RUN)) {
            if (lotInfo != null) {
                ClientHandler.sendMessage(evtNo, false, 2, "[Client-Eap]KVM上报设备状态切换为Run");
                lastState = "1";
            } else {
                ClientHandler.sendMessage(evtNo, false, 1, "未找到相应的制程信息");
                return;
            }
        } else {
            if (lotInfo != null) {
                eqptStat = EqptStat.DOWN;
                lastState = "3";
                ClientHandler.sendMessage(evtNo, false, 2, "[Client-Eap]KVM上报设备状态切换为Down");
            } else {
                eqptStat = EqptStat.IDLE;
                lastState = "2";
                ClientHandler.sendMessage(evtNo, false, 1, "[Client-Eap]KVM上报设备状态切换为Idle");
            }
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上报设备状态信息指令成功");
        EMSStatusReportO emsStatusReportO = EmsHandler.emsStatusReportToEms(evtNo, eqptInfo.getEqptMode(), lastState, eqptStat,lotInfo.getLotId());
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
            eapSyncEqpInfoI.setProbeCardId(lotInfo.getProberCard());
        }
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
    }

    private void operateEnd(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        String opeType = inTrx.getOpeType();
        switch (opeType) {
            case "A":  //Prober LOT信息写入后，自动化完成
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
        if(!RETURN_CODE_OK.equals(outTrx.getRtnCode())){
            LotInfo lotInfo = lotDao.getCurLotInfo();
            if(lotInfo == null){
                return;
            }
            EAPEndCardO eapEndCardO = KvmHandler.eapEndCard(evtNo);
            if(!RETURN_CODE_OK.equals(eapEndCardO.getRtnCode())){
                ClientHandler.sendMessage(evtNo, false, MessageType.ERROR, eapEndCardO.getRtnMesg());
            }

            lotDao.removeLotInfo();
            stateDao.removeState();

            EqptInfo eqptInfo =  eqptDao.getEqpt();
            if(eqptInfo == null){
                eqptInfo = new EqptInfo();
                eqptInfo.setEqptId(equipmentNo);
                eqptInfo.setEqptMode(EqptMode.Online);
            }
            eqptInfo.setEqptStat(EqptStat.IDLE);
            eqptDao.addEqpt(eqptInfo);
            MesHandler.eqptStatReport(evtNo, EqptStat.IDLE, "无", lotInfo.getUserId());
            RcmHandler.eqptInfoReport(evtNo, lotInfo.getLotId(), EqptStat.IDLE, _SPACE, _SPACE,_SPACE, _SPACE);
        }
    }

    private void realTimeTemperatureCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            eapEndCard(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg(inTrx.getOpeContent());
            eapEndCard(evtNo);
            ClientHandler.sendMessage(evtNo, false, 2,"[EAP-KVM]: " + outTrx.getRtnMesg());
            return;
        }
        String eqptDeviceTemperature = inTrx.getOpeContent();
        // 使用正则表达式提取数字部分
        String numericPart = eqptDeviceTemperature.replaceAll("[^\\d.]", "");

        LogUtils.info("KVM采集上报的温度为：" +numericPart);

        String temperatureRang = lotInfo.getTemperatureRange();
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = lotInfo.getTemperature();
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if (value <= tem - tempRang || value >= tem + tempRang) {
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            eapEndCard(evtNo);
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
        EAPReqLotInfoOB clientLotInfo = lotDao.getClientLotInfo();
        String lotId = lotInfo.getLotId();

        // 第六步DEVICENAME参数写入开始
        clientHandler.setFlowStep(StepName.SIXTH,StepStat.INPROCESS);
        HashMap<String, String > stringMap  = new HashMap<String, String>(){{
            put("WaferLot",lotInfo.getWaferLot());
        }};
        lotInfo.setParamMap(stringMap);
        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
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
        String returnMesg2 = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
        Stateset("5", "1", lotId);
        if (StringUtils.isEmpty(returnMesg2)) {
            Stateset("5", "3", lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发指令， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg2, EAPOperationInstructionO.class);
        if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
            Stateset("5", "3", lotId);
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            eapEndCard(evtNo);
            removeCache();
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP发送代操采集指令成功。");
    }


    private void deviceNameVerfication(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        StateInfo stateInfo = stateDao.getStateInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg(inTrx.getOpeContent());
            eapEndCard(evtNo);
            removeCache();
            ClientHandler.sendMessage(evtNo, false, 1,"[EAP-KVM]: " + outTrx.getRtnMesg());
            return;
        }
        if(stateInfo == null){
            stateInfo = new StateInfo();
            stateInfo.setLotNo(lotInfo.getLotId());
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

                    ClientHandler.sendMessage(evtNo, true, 1, outTrx.getRtnMesg());
                    return;
                }
                ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:KVM Device Name采集校验成功，设备Device Name:[" + eqptDeviceName + "], 批次Device:[" + lotDeviceName + "]");
                EmsHandler.reportRunWorkInfo(evtNo,"DeviceName校验完成",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());

                //第三步DeviceName校验完成
                clientHandler.setFlowStep(StepName.THIRD,StepStat.COMP);
            }
        }
    }

    /**
     * @param evtNo
     * @param inTrx
     * @param outTrx 模式设置，Fail报警，模式选择等操作完成
     */
    private void deviceParamCollection(String evtNo, KVMOperateEndI inTrx, KVMOperateEndO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        StateInfo stateInfo = stateDao.getStateInfo();
        if(stateInfo == null){
            stateInfo = new StateInfo();
        }
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }
        String lotId = lotInfo.getLotId();
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg(inTrx.getOpeContent());
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]: " + outTrx.getRtnMesg());
            return;
        }

        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:自动化作业完成。");
        EmsHandler.reportRunWorkInfo(evtNo,"KVM自动化完成",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
        //第六步DEVICENAME参数写入结束
        clientHandler.setFlowStep(StepName.SIXTH,StepStat.INPROCESS);
        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
        eapSingleParamCollectionI.setActionFlg("RPS");
        eapSingleParamCollectionI.setParameterName("");
        eapSingleParamCollectionI.setRequestKey(evtNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
        if (StringUtils.isEmpty(returnMesg)) {
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 没有返回");
            return;
        }
        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
        if (!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 发送采集Device 温度， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
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
                outTrx.setRtnMesg(inTrx.getOpeContent());
                ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]: " + outTrx.getRtnMesg());
                return;
            }

            String responseKey = inTrx.getResponseKey();
            String recipeBody = inTrx.getOpeContent();
            if (AsyncUtils.existRequest(responseKey)) {
                AsyncUtils.setResponse(responseKey, recipeBody);
            } else {
                ClientHandler.sendMessage(evtNo, false, 2, "KVM采集Device param完成。");
                if (rmsCheckFlag) {
                    RmsOnlineValidationO rmsOnlineValidationO = RMSHandler.toRmsOnlineValidation(evtNo, equipmentNo, lotInfo.getLotId(), lotInfo.getDevice(),"CP");
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
        //第七步温度校验开始
        clientHandler.setFlowStep(StepName.SEVENTH,StepStat.INPROCESS);
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            return;
        }
        String lotId = lotInfo.getLotId();
        String state = inTrx.getState();
        if (KVMOperateState.Fail.equals(state)) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg(inTrx.getOpeContent());
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:" + outTrx.getRtnMesg());
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
        if (value < tem - tempRang || value > tem + tempRang) {
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("KVM采集的温度:[" + numericPart + "]不在批次:[" + lotInfo.getLotId() + "]温度:[" + temperature + "]上下" + temperatureRang + "的范围内");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "温度采验成功，设备Device 温度:[" + numericPart + "], 批次Device的温度:[" + temperature + "],在该温度范围内");
        EmsHandler.reportRunWorkInfo(evtNo,"温度校验成功",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
        //第七步温度校验结束
        clientHandler.setFlowStep(StepName.SEVENTH,StepStat.COMP);

        //第八步SlotMap开始
        clientHandler.setFlowStep(StepName.EIGTH,StepStat.INPROCESS);
        //eap给kvm下发指令执行slotmap
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KVMSlotmapModeI kvmSlotmapModeI = new KVMSlotmapModeI();
        kvmSlotmapModeI.setTrxId("EAPACCEPT");
        kvmSlotmapModeI.setActionFlg("SLT");
        kvmSlotmapModeI.setEqpId(equipmentNo);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmSlotmapModeI);
        if (StringUtils.isEmpty(returnMesg)) {
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发slotmap指令， KVM 没有返回");
            return;
        }
        KVMSlotmapModeO kvmSlotmapModeO = JacksonUtils.string2Object(returnMesg, KVMSlotmapModeO.class);
        if (!RETURN_CODE_OK.equals(kvmSlotmapModeO.getRtnCode())) {
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发slotmap指令， KVM 返回错误:[" + kvmSlotmapModeO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP发送slotMap采集指令成功。");

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
            Stateset("10", "3", lotId);
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg(inTrx.getOpeContent());
            eapEndCard(evtNo);
            removeCache();
            ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]: " + outTrx.getRtnMesg());
            return;
        }
        String eqpTestProgram = inTrx.getOpeContent();
        File file = new File(eqpTestProgram);
        String name = file.getName();
        int i = name.indexOf(".");
        String eapTestName = name.substring(0, i);
        LogUtils.info("KVM返回测程式为：" + eapTestName);
        String lotTestProgram = lotInfo.getTestProgram();
        if (!lotTestProgram.equals(eapTestName)) {
            Stateset("10", "3", lotId);
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            outTrx.setRtnCode(DEVICE_DISMATCH);
            outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "] TestProgram:[" + lotTestProgram + "]与KVM采集的TestProgram:[" + eapTestName + "]不匹配，请确认");
            eapEndCard(evtNo);
            removeCache();
            return;
        }
        ClientHandler.sendMessage(evtNo, true, 2, "KVM返回的程式为: " + eqpTestProgram);
        ClientHandler.sendMessage(evtNo, true, 2, "MES下发的程式为: " + lotTestProgram);
        ClientHandler.sendMessage(evtNo, true, 2, "KVM测试程式验证通过，请点击prober机台start按钮开始作业");
        Stateset("10", "2", lotId);

        Path dataPath = Paths.get(System.getProperty("user.dir"), "data", "MES_DATA" + ".TXT");
        Path dataPath1 = Paths.get(System.getProperty("user.dir"), "data", "MES_TEST_DATA" + ".TXT");
        if (!Files.exists(dataPath.getParent())) {
            try {
                Files.createDirectories(dataPath.getParent());
            } catch (IOException e) {
                LogUtils.error("文件夹创建失败");
            }
        }
        LogUtils.info("CheckIn结束，开始生成测试文件");
        StringBuilder sb = new StringBuilder();
        String sampleValue = null;
        String custLotNo = null;

        String[] split = new String[0];
        List<EAPReqLotInfoOB> paramList = lotInfo.getParamList();
        for (EAPReqLotInfoOB eapReqLotInfoOB : paramList){
            if ("Sample".equals(eapReqLotInfoOB.getParamName())){
                sampleValue = eapReqLotInfoOB.getParamValue();
            }
            if ("CustLotNo".equals(eapReqLotInfoOB.getParamName())){
                custLotNo = eapReqLotInfoOB.getParamValue();
            }
        }
        EAPReqLotInfoOC eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
        if (eapReqLotInfoOC !=null){
            split = eapReqLotInfoOC.getDatas().split(",");
        }
        sb.append("TP_NAME:").append(lotInfo.getTestProgram()).append("\n")
                .append("TESTER_ID:").append(testEqp).append("\n")
                .append("PART_NO:").append("NA").append("\n")
                .append("QTY:").append(split.length).append("\n")
                .append("C_LOT:").append(custLotNo).append("\n")
                .append("HANDLER:").append("NA").append("\n")
                .append("PROBER:").append(proberName).append("\n")
                .append("LOADBOARD:").append("NA").append("\n")
                .append("PROBERCARD:").append(lotInfo.getProberCard()).append("\n")
                .append("SOCKET:").append("NA").append("\n")
                .append("PROCESS:").append("CP").append("\n")
                .append("STEP:").append("CP1").append("\n")
                .append("RETEST:").append("NA").append("\n")
                .append("RT_BIN:").append("NA").append("\n")
                .append("SUBCON_NAME:").append("NA").append("\n")
                .append("SUBCON_LOT:").append("NA").append("\n")
                .append("DATE_CODE:").append("NA").append("\n")
                .append("OPID:").append(lotInfo.getUserId()).append("\n")
                .append("PASS_BIN:").append("NA").append("\n")
                .append("WORK_MODE:").append("TEST").append("\n")
                .append("EXTENSION:").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"))).append("\n");
        LogUtils.info("CheckIn生成的数据为：" + sb);
        KvmHandler.reportMESData(evtNo,sb.toString());
        LogUtils.info("EAP上传MES_DATA到RCM成功！");
        try {
            Files.write(dataPath, sb.toString().getBytes());
            Files.write(dataPath1, sb.toString().getBytes());
            FtpUtils.uploadFile(host, user, password, port, path, dataPath.toString());
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

    public void eapEndCard(String evtNo) {
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
    }

    public void removeCache() {
        lotDao.removeLotInfo();
        stateDao.removeState();
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

