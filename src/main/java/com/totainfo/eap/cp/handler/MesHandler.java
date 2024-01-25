package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.service.kvm.KVMOperateEndService;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptStatusReport.EAPEqptStatusReportI;
import com.totainfo.eap.cp.trx.mes.EAPEqptStatusReport.EAPEqptStatusReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoI;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultI;
import com.totainfo.eap.cp.trx.mes.EAPReqMeasureResult.EAPReqMeasureResultO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultI;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultIA;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultI;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultO;
import com.totainfo.eap.cp.trx.mes.EAPSyncProberCard.MESSyncProberCardI;
import com.totainfo.eap.cp.trx.mes.EAPSyncProberCard.MESSyncProberCardO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.MES_TIME_OUT;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:36
 */
@Component
public class MesHandler {
    private static RabbitmqHandler rabbitmqHandler;
    private static final String appName = "MES";

    private static String mesQueue;
    private static  String mesExchange;
    private static String computerName;
    static {
        InetAddress address = null; // 此处可以是计算机名或者IP，任一即可
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LogUtils.error("获取服务器名失败", e);
        }
        computerName = address.getHostName();
    }



    public static EAPEqptAlarmReportO alarmReport(String evtNo,String alarmCode, String alarmText, String time,String ID){
        EAPEqptAlarmReportI eapEqptAlarmReportI = new EAPEqptAlarmReportI();
        EAPEqptAlarmReportO eapEqptAlarmReportO = new EAPEqptAlarmReportO();
        eapEqptAlarmReportI.setTrxId("uploadEquipmentErrorMessage");
        eapEqptAlarmReportI.setEvtUsr("");
        eapEqptAlarmReportI.setComputerName(computerName);
        eapEqptAlarmReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        eapEqptAlarmReportI.setErrorcode(alarmCode);
        eapEqptAlarmReportI.setMessage(alarmText);
        eapEqptAlarmReportI.setTime(time);
        eapEqptAlarmReportI.setId(ID);

        String inTrxStr = JacksonUtils.object2String(eapEqptAlarmReportI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapEqptAlarmReportI);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptAlarmReportO.setRtnCode("00000001");
            eapEqptAlarmReportO.setRtnMesg("[EAP-MES]:EAP发送设备报警，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapEqptAlarmReportO.getRtnMesg());
        }else{
            eapEqptAlarmReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptAlarmReportO.class);
        }
        return eapEqptAlarmReportO;
    }

    public static EAPEqptStatusReportO eqptStatReport(String evtNo, String eqptStat, String remark, String userId){
        EAPEqptStatusReportI eapEqptStatusReportI = new EAPEqptStatusReportI();
        EAPEqptStatusReportO eapEqptStatusReportO = new EAPEqptStatusReportO();
        eapEqptStatusReportI.setTrxId("equipmentStateChange");
        eapEqptStatusReportI.setEvtUsr(userId);
        eapEqptStatusReportI.setComputerName(computerName);
        eapEqptStatusReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        eapEqptStatusReportI.setLastState(eqptStat);
        eapEqptStatusReportI.setRemark(remark);

        String inTrxStr = JacksonUtils.object2String(eapEqptStatusReportI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapEqptStatusReportI);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptStatusReportO.setRtnCode(MES_TIME_OUT);
            eapEqptStatusReportO.setRtnMesg("[EAP-MES]:EAP发送设备状态，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapEqptStatusReportO.getRtnMesg());
        }else{
            eapEqptStatusReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptStatusReportO.class);
        }
        return eapEqptStatusReportO;
    }

    public static EAPReqLotInfoO lotInfoReq(String evtNo, String lotNo, String probeCardId,String userId){
        EAPReqLotInfoI eapReqLotInfoI = new EAPReqLotInfoI();
        EAPReqLotInfoO eapReqLotInfoO = new EAPReqLotInfoO();
        eapReqLotInfoI.setTrxId("getLotInfoByLotNoAndEquipmentNo");
        eapReqLotInfoI.setEvtUsr(userId);
        eapReqLotInfoI.setLotNo(lotNo);
        String[] split = probeCardId.split("-");
        eapReqLotInfoI.setProbeCardId(split[0]);
        eapReqLotInfoI.setComputerName(computerName);
        eapReqLotInfoI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqLotInfoI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqLotInfoO.setRtnCode(MES_TIME_OUT);
            eapReqLotInfoO.setRtnMesg("[EAP-MES]:EAP发送设备状态，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapReqLotInfoO.getRtnMesg());
        }else{
            eapReqLotInfoO = JacksonUtils.string2Object(outTrxStr, EAPReqLotInfoO.class);
        }
        return eapReqLotInfoO;
    }


    public static EAPReqCheckInO checkInReq(String evtNo, String lotNo, String userId,EAPReqCheckInI eapReqCheckInI){
//        EAPReqCheckInI eapReqCheckInI = new EAPReqCheckInI();
        EAPReqCheckInO eapReqCheckInO = new EAPReqCheckInO();
        eapReqCheckInI.setComputerName(computerName);
        eapReqCheckInI.setEquipmentNo(GenericDataDef.equipmentNo);
        LogUtils.info("准备往mes发checkin");
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqCheckInI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckInO.setRtnCode(MES_TIME_OUT);
            eapReqCheckInO.setRtnMesg("[EAP-MES]:EAP发送Lot:["+lotNo+"] Check In，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapReqCheckInO.getRtnMesg());
        }else{
            eapReqCheckInO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckInO.class);
        }
        return eapReqCheckInO;
    }



    public static EAPReqCheckOutO checkOutReq(String evtNo, String lotNo){
        EAPReqCheckOutI eapReqCheckOutI = new EAPReqCheckOutI();
        EAPReqCheckOutO eapReqCheckOutO = new EAPReqCheckOutO();
        eapReqCheckOutI.setTrxId("checkOut");
        eapReqCheckOutI.setEvtUsr("");
        eapReqCheckOutI.setLotNo(lotNo);
        eapReqCheckOutI.setComputerName(computerName);
        eapReqCheckOutI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqCheckOutI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckOutO.setRtnCode(MES_TIME_OUT);
            eapReqCheckOutO.setRtnMesg("[EAP-MES]:EAP发送Lot:["+lotNo+"] Check Out，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapReqCheckOutO.getRtnMesg());
        }else{
            eapReqCheckOutO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckOutO.class);
        }
        return eapReqCheckOutO;
    }

    public static EAPReqMeasureResultO measureResultReq(String evtNo, String lotNo){
        EAPReqMeasureResultI eapReqMeasureResultI = new EAPReqMeasureResultI();
        EAPReqMeasureResultO eapReqMeasureResultO = new EAPReqMeasureResultO();
        eapReqMeasureResultI.setTrxId("RequestMeasurationResult");
        eapReqMeasureResultI.setEvtUsr("");
        eapReqMeasureResultI.setLotNo(lotNo);
        eapReqMeasureResultI.setComputerName(computerName);
        eapReqMeasureResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqMeasureResultI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqMeasureResultI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqMeasureResultO.setRtnCode(MES_TIME_OUT);
            eapReqMeasureResultO.setRtnMesg("[EAP-MES]:EAP请求Lot:["+lotNo+"] 量测结果，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapReqMeasureResultO.getRtnMesg());
        }else{
            eapReqMeasureResultO = JacksonUtils.string2Object(outTrxStr, EAPReqMeasureResultO.class);
        }
        return eapReqMeasureResultO;
    }


    public static EAPUploadDieResultO uploadDieResult(String evtNo, String lotNo, String waferId, List<DielInfo> datas, String userId){
        EAPUploadDieResultI eapUploadDieResultI = new EAPUploadDieResultI();
        EAPUploadDieResultO eapUploadDieResultO = new EAPUploadDieResultO();
        eapUploadDieResultI.setTrxId("uploaddietestresult");
        eapUploadDieResultI.setEvtUsr(userId);
        eapUploadDieResultI.setLotNo(lotNo);
        eapUploadDieResultI.setWaferId(waferId);

        List<EAPUploadDieResultIA> eapUploadDieResultIAS = new ArrayList<>(datas.size());
        for(DielInfo dielInfo:datas){
            EAPUploadDieResultIA eapUploadDieResultIA = new EAPUploadDieResultIA();
            eapUploadDieResultIA.setStartCoorDinates(dielInfo.getCoordinate());
            eapUploadDieResultIA.setResult(dielInfo.getResult());
            eapUploadDieResultIAS.add(eapUploadDieResultIA);
        }
        eapUploadDieResultI.setDatas(eapUploadDieResultIAS);

        eapUploadDieResultI.setComputerName(computerName);
        eapUploadDieResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapUploadDieResultI);
        if(!StringUtils.hasText(outTrxStr)){
            eapUploadDieResultO.setRtnCode(MES_TIME_OUT);
            eapUploadDieResultO.setRtnMesg("[EAP-MES]:EAP上传Lot:["+lotNo+"],Wafer:["+waferId+"],DIE 测试结果，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapUploadDieResultO.getRtnMesg());
        }else{
            eapUploadDieResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadDieResultO.class);
        }
        return eapUploadDieResultO;
    }

    public static EAPUploadMarkResultO uploadMarkResult(String evtNo, String lotNo, String waferId,String remark,List datas, String userId) {
        EAPUploadMarkResultI eapUploadMarkResultI = new EAPUploadMarkResultI();
        EAPUploadMarkResultO eapUploadMarkResultO = new EAPUploadMarkResultO();
        eapUploadMarkResultI.setTrxId("uploadNeedleMarkResults");
        eapUploadMarkResultI.setEvtUsr(userId);
        eapUploadMarkResultI.setLotNo(lotNo);
        eapUploadMarkResultI.setWaferId(waferId);
        eapUploadMarkResultI.setDatas(datas);
        eapUploadMarkResultI.setRemark(remark);
        eapUploadMarkResultI.setComputerName(computerName);
        eapUploadMarkResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, eapUploadMarkResultI);
        if (!StringUtils.hasText(outTrxStr)) {
            eapUploadMarkResultO.setRtnCode(MES_TIME_OUT);
            eapUploadMarkResultO.setRtnMesg("[EAP-MES]:EAP上传Lot:[" + lotNo + "],Wafer:[" + waferId + "] 针痕检测结果，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapUploadMarkResultO.getRtnMesg());
        } else {
            eapUploadMarkResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadMarkResultO.class);
        }
        return eapUploadMarkResultO;
    }

    public static MESSyncProberCardO syncProberCardInfo(String evtNo, String userId, String proberCardId,String lotNo){
        MESSyncProberCardI mesSyncProberCardI = new MESSyncProberCardI();
        MESSyncProberCardO mesSyncProberCardO = new MESSyncProberCardO();
        mesSyncProberCardI.setTrxId("SynchronousMESProberCardId");
        mesSyncProberCardI.setActionFlg("");
        mesSyncProberCardI.setEquipmentNo(GenericDataDef.equipmentNo);
        mesSyncProberCardI.setComputerName(computerName);
        mesSyncProberCardI.setEvtUsr(userId);
        mesSyncProberCardI.setProberCardId(proberCardId);
        mesSyncProberCardI.setLotNo(lotNo);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, mesSyncProberCardI);
        if (!StringUtils.hasText(outTrxStr)) {
            mesSyncProberCardO.setRtnCode(MES_TIME_OUT);
            mesSyncProberCardO.setRtnMesg("[EAP-MES]:EAP同步探针:["+proberCardId+"] 信息，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,mesSyncProberCardO.getRtnMesg());
        } else {
            mesSyncProberCardO = JacksonUtils.string2Object(outTrxStr, MESSyncProberCardO.class);
        }
        return mesSyncProberCardO;
    }

    public static GPIBLotStartReportO lotStart(String evtNo,String evtUsr,String lotNo){
        GPIBLotStartReportI gpibLotStartReportI = new GPIBLotStartReportI();
        GPIBLotStartReportO gpibLotStartReportO = new GPIBLotStartReportO();
        gpibLotStartReportI.setTrxId("LotStart");
        gpibLotStartReportI.setComputerName(computerName);
        gpibLotStartReportI.setEvtUsr(evtUsr);
        gpibLotStartReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        gpibLotStartReportI.setLotNo(lotNo);
        String reply = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, gpibLotStartReportI);
        if(!StringUtils.hasText(reply)){
            gpibLotStartReportO.setRtnCode(MES_TIME_OUT);
            gpibLotStartReportO.setRtnMesg("[EAP-MES]:EAP上报LotStart信息，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,gpibLotStartReportO.getRtnMesg());
        }else {
            gpibLotStartReportO = JacksonUtils.string2Object(reply, GPIBLotStartReportO.class);
        }
        return gpibLotStartReportO;
    }

    public static GPIBLotEndReportO lotEnd(String evtNo,String evtUsr,String LotNo){
        GPIBLotEndReportI gpibLotEndReportI = new GPIBLotEndReportI();
        GPIBLotEndReportO gpibLotEndReportO = new GPIBLotEndReportO();
        gpibLotEndReportI.setTrxId("LotEnd");
        gpibLotEndReportI.setComputerName(computerName);
        gpibLotEndReportI.setEvtUsr(evtUsr);
        gpibLotEndReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        gpibLotEndReportI.setLotNo(LotNo);
        String reply = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, gpibLotEndReportI);
        if(!StringUtils.hasText(reply)){
            gpibLotEndReportO.setRtnCode(MES_TIME_OUT);
            gpibLotEndReportO.setRtnMesg("[EAP-MES]:EAP上报LotEnd信息，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,gpibLotEndReportO.getRtnMesg());
        }else {
            gpibLotEndReportO = JacksonUtils.string2Object(reply, GPIBLotEndReportO.class);
        }
        return gpibLotEndReportO;

    }

    public static GPIBWaferStartReportO waferStart(String evtNo,String evtUsr,String lotNo,String waferId){
        GPIBWaferStartReportI gpibWaferStartReportI = new GPIBWaferStartReportI();
        GPIBWaferStartReportO gpibWaferStartReportO = new GPIBWaferStartReportO();
        gpibWaferStartReportI.setTrxId("WaferStart");
        gpibWaferStartReportI.setComputerName(computerName);
        gpibWaferStartReportI.setEvtUsr(evtUsr);
        gpibWaferStartReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        gpibWaferStartReportI.setLotNo(lotNo);
        gpibWaferStartReportI.setWaferId(waferId);
        String reply = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, gpibWaferStartReportI);
        if (!StringUtils.hasText(reply)){
            gpibWaferStartReportO.setRtnCode(MES_TIME_OUT);
            gpibWaferStartReportO.setRtnMesg("[EAP-MES]:EAP上报WaferStart信息，MES没有回复");
            ClientHandler.sendMessage(evtNo,false,1,gpibWaferStartReportO.getRtnMesg());
        }else {
            gpibWaferStartReportO = JacksonUtils.string2Object(reply, GPIBWaferStartReportO.class);
        }
        return gpibWaferStartReportO;
    }


    @Autowired
    public void setRabbitmqHandler(RabbitmqHandler rabbitmqHandler) {

        MesHandler.rabbitmqHandler = rabbitmqHandler;
    }

    @Value("${spring.rabbitmq.mes.queue}")
    public void setMesQueue(String queue) {
        MesHandler.mesQueue = queue;
    }

    @Value("${spring.rabbitmq.mes.exchange}")
    public void setMesExchange(String exchange) {
        MesHandler.mesExchange = exchange;
    }

}
