package com.totainfo.eap.cp.handler;

import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.IEapBaseInterface;
import com.totainfo.eap.cp.commdef.GenericDataDef;
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
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultI;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.MatrixAppContext;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.totainfo.eap.cp.commdef.GenericDataDef.*;

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



    public static EAPEqptAlarmReportO alarmReport(String evtNo,String alarmCode, String alarmText, String time){
        EAPEqptAlarmReportI eapEqptAlarmReportI = new EAPEqptAlarmReportI();
        EAPEqptAlarmReportO eapEqptAlarmReportO = new EAPEqptAlarmReportO();
        eapEqptAlarmReportI.setEvtUsr("");
        eapEqptAlarmReportI.setComputerName(computerName);
        eapEqptAlarmReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        eapEqptAlarmReportI.setErrorcode(alarmCode);
        eapEqptAlarmReportI.setMessage(alarmText);
        eapEqptAlarmReportI.setTime(time);

        String inTrxStr = JacksonUtils.object2String(eapEqptAlarmReportI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapEqptAlarmReportI);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptAlarmReportO.setRtnCode("00000001");
            eapEqptAlarmReportO.setRtnMesg("EAP发送设备报警，MES没有回复");
        }else{
            eapEqptAlarmReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptAlarmReportO.class);
        }
        return eapEqptAlarmReportO;
    }

    public static EAPEqptStatusReportO eqptStatReport(String evtNo, String eqptStat, String remark){
        EAPEqptStatusReportI eapEqptStatusReportI = new EAPEqptStatusReportI();
        EAPEqptStatusReportO eapEqptStatusReportO = new EAPEqptStatusReportO();
        eapEqptStatusReportI.setEvtUsr("");
        eapEqptStatusReportI.setComputerName(computerName);
        eapEqptStatusReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        eapEqptStatusReportI.setLastState(eqptStat);
        eapEqptStatusReportI.setRemark(remark);

        String inTrxStr = JacksonUtils.object2String(eapEqptStatusReportI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapEqptStatusReportI);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptStatusReportO.setRtnCode(MES_TIME_OUT);
            eapEqptStatusReportO.setRtnMesg("EAP发送设备状态，MES没有回复");
        }else{
            eapEqptStatusReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptStatusReportO.class);
        }
        return eapEqptStatusReportO;
    }

    public static EAPReqLotInfoO lotInfoReq(String evtNo, String lotNo, String proberId,String userId){
        EAPReqLotInfoI eapReqLotInfoI = new EAPReqLotInfoI();
        EAPReqLotInfoO eapReqLotInfoO = new EAPReqLotInfoO();
        eapReqLotInfoI.setEvtUsr(userId);
        eapReqLotInfoI.setLotNo(lotNo);
        eapReqLotInfoI.setProberId(proberId);
        eapReqLotInfoI.setComputerName(computerName);
        eapReqLotInfoI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqLotInfoI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqLotInfoO.setRtnCode(MES_TIME_OUT);
            eapReqLotInfoO.setRtnMesg("EAP发送设备状态，MES没有回复");
        }else{
            eapReqLotInfoO = JacksonUtils.string2Object(outTrxStr, EAPReqLotInfoO.class);
        }
        return eapReqLotInfoO;
    }


    public static EAPReqCheckInO checkInReq(String evtNo, String lotNo, String userId){
        EAPReqCheckInI eapReqCheckInI = new EAPReqCheckInI();
        EAPReqCheckInO eapReqCheckInO = new EAPReqCheckInO();
        eapReqCheckInI.setEvtUsr(userId);
        eapReqCheckInI.setLotNo(lotNo);
        eapReqCheckInI.setComputerName(computerName);
        eapReqCheckInI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqCheckInI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckInO.setRtnCode(MES_TIME_OUT);
            eapReqCheckInO.setRtnMesg("EAP发送Lot:["+lotNo+"] Check In，MES没有回复");
        }else{
            eapReqCheckInO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckInO.class);
        }
        return eapReqCheckInO;
    }



    public static EAPReqCheckOutO checkOutReq(String evtNo, String lotNo){
        EAPReqCheckOutI eapReqCheckOutI = new EAPReqCheckOutI();
        EAPReqCheckOutO eapReqCheckOutO = new EAPReqCheckOutO();
        eapReqCheckOutI.setEvtUsr("");
        eapReqCheckOutI.setLotNo(lotNo);
        eapReqCheckOutI.setComputerName(computerName);
        eapReqCheckOutI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqCheckOutI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckOutO.setRtnCode(MES_TIME_OUT);
            eapReqCheckOutO.setRtnMesg("EAP发送Lot:["+lotNo+"] Check In，MES没有回复");
        }else{
            eapReqCheckOutO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckOutO.class);
        }
        return eapReqCheckOutO;
    }


    public static EAPReqMeasureResultO measureResultReq(String evtNo, String lotNo){
        EAPReqMeasureResultI eapReqMeasureResultI = new EAPReqMeasureResultI();
        EAPReqMeasureResultO eapReqMeasureResultO = new EAPReqMeasureResultO();
        eapReqMeasureResultI.setEvtUsr("");
        eapReqMeasureResultI.setLotNo(lotNo);
        eapReqMeasureResultI.setComputerName(computerName);
        eapReqMeasureResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqMeasureResultI);
        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapReqMeasureResultI);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqMeasureResultO.setRtnCode(MES_TIME_OUT);
            eapReqMeasureResultO.setRtnMesg("EAP请求Lot:["+lotNo+"] 量测结果，MES没有回复");
        }else{
            eapReqMeasureResultO = JacksonUtils.string2Object(outTrxStr, EAPReqMeasureResultO.class);
        }
        return eapReqMeasureResultO;
    }


    public static EAPUploadDieResultO uploadDieResult(String evtNo, String lotNo, String waferId, String startCoordinates, String result, String userId){
        EAPUploadDieResultI eapUploadDieResultI = new EAPUploadDieResultI();
        EAPUploadDieResultO eapUploadDieResultO = new EAPUploadDieResultO();
        eapUploadDieResultI.setEvtUsr(userId);
        eapUploadDieResultI.setLotNo(lotNo);
        eapUploadDieResultI.setWaferId(waferId);
        eapUploadDieResultI.setStartingCoordinates(startCoordinates);
        eapUploadDieResultI.setResult(result);
        eapUploadDieResultI.setComputerName(computerName);
        eapUploadDieResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr =rabbitmqHandler.sendForReply (evtNo,appName,mesQueue, mesExchange, eapUploadDieResultI);
        if(!StringUtils.hasText(outTrxStr)){
            eapUploadDieResultO.setRtnCode(MES_TIME_OUT);
            eapUploadDieResultO.setRtnMesg("EAP上传Lot:["+lotNo+"],Wafer:["+waferId+"],DIE 测试结果，MES没有回复");
        }else{
            eapUploadDieResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadDieResultO.class);
        }
        return eapUploadDieResultO;
    }

    public static EAPUploadMarkResultO uploadMarkResult(String evtNo, String lotNo, String waferId, String startCoordinates, String result, String remark, String userId) {
        EAPUploadMarkResultI eapUploadMarkResultI = new EAPUploadMarkResultI();
        EAPUploadMarkResultO eapUploadMarkResultO = new EAPUploadMarkResultO();
        eapUploadMarkResultI.setEvtUsr(userId);
        eapUploadMarkResultI.setLotNo(lotNo);
        eapUploadMarkResultI.setWaferId(waferId);
        eapUploadMarkResultI.setStartingCoordinates(startCoordinates);
        eapUploadMarkResultI.setResult(result);
        eapUploadMarkResultI.setRemark(remark);
        eapUploadMarkResultI.setComputerName(computerName);
        eapUploadMarkResultI.setEquipmentNo(GenericDataDef.equipmentNo);

        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, mesQueue, mesExchange, eapUploadMarkResultI);
        if (!StringUtils.hasText(outTrxStr)) {
            eapUploadMarkResultO.setRtnCode(MES_TIME_OUT);
            eapUploadMarkResultO.setRtnMesg("EAP上传Lot:[" + lotNo + "],Wafer:[" + waferId + "] 针痕检测结果，MES没有回复");
        } else {
            eapUploadMarkResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadMarkResultO.class);
        }
        return eapUploadMarkResultO;
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
