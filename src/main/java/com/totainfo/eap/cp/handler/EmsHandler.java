package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.trx.ems.EAPRepRunWorkInfo.EAPRepRunWorkInfoI;
import com.totainfo.eap.cp.trx.ems.EAPRepRunWorkInfo.EAPRepRunWorkInfoO;
import com.totainfo.eap.cp.trx.ems.EMSAlarmReport.EMSAlarmReportI;
import com.totainfo.eap.cp.trx.ems.EMSAlarmReport.EMSAlarmReportO;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportIA;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportO;
import com.totainfo.eap.cp.trx.ems.EMSGetAlarm.EMSGetAlarmI;
import com.totainfo.eap.cp.trx.ems.EMSGetAlarm.EMSGetAlarmO;
import com.totainfo.eap.cp.trx.ems.EMSGetTestResult.EMSGetTestResultI;
import com.totainfo.eap.cp.trx.ems.EMSGetTestResult.EMSGetTestResultO;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportO;
import com.totainfo.eap.cp.trx.ems.EMSStatusReport.EMSStatusReportI;
import com.totainfo.eap.cp.trx.ems.EMSStatusReport.EMSStatusReportO;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class EmsHandler {
    private static RabbitmqHandler rabbitmqHandler;
    private static final String appName = "EMS";

    private static String emsQueue;
    private static  String emsExchange;
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
    //上报设备报警信息
    public static EMSAlarmReportO alarmReportToEms(String evtNo,String alarmCode, String alarmMessage,String lotNo,String alarmTab,String alarmImg){
        EMSAlarmReportI emsAlarmReportI = new EMSAlarmReportI();
        EMSAlarmReportO emsAlarmReportO = new EMSAlarmReportO();
        emsAlarmReportI.setTrxId("reportEqpAlarm");
        emsAlarmReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsAlarmReportI.setLotNo(lotNo);
        emsAlarmReportI.setAlarmTab(alarmTab);
        emsAlarmReportI.setAlarmCode(alarmCode);
        emsAlarmReportI.setAlarmMessage(alarmMessage);
        emsAlarmReportI.setAlarmImg(alarmImg);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsAlarmReportI);
        if(!StringUtils.hasText(outTrxStr)){
            emsAlarmReportO.setRtnCode("00000001");
            emsAlarmReportO.setRtnMesg("[EAP-MES]:EAP上报设备报警信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsAlarmReportO.getRtnMesg());
        }else{
            emsAlarmReportO = JacksonUtils.string2Object(outTrxStr,EMSAlarmReportO.class);
        }
        return emsAlarmReportO;
    }
    //上报设备状态给ems
    public static EMSStatusReportO emsStatusReportToEms(String evtNo,String eqpCommStatus,String lastState,String lastStateVal,String lotNo){

        EMSStatusReportI emsStatusReportI = new EMSStatusReportI();
        EMSStatusReportO emsStatusReportO = new EMSStatusReportO();
        emsStatusReportI.setTrxId("reportEqpStateChange");
        emsStatusReportI.setEqpCommStatus(eqpCommStatus);
        emsStatusReportI.setLastState(lastState);
        emsStatusReportI.setLastStateVal(lastStateVal);
        emsStatusReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsStatusReportI.setLotNo(lotNo);
        emsStatusReportI.setRemark("");
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsStatusReportI);
        if(!StringUtils.hasText(outTrxStr)){
            emsStatusReportO.setRtnCode("00000001");
            emsStatusReportO.setRtnMesg("[EAP-MES]:EAP上报设备状态信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsStatusReportO.getRtnMesg());
        }else{
            emsStatusReportO = JacksonUtils.string2Object(outTrxStr,EMSStatusReportO.class);
        }
        return emsStatusReportO;
    }
    //上传生产相关的所有信息
    public static EMSLotInfoReportO emsLotInfoReporToEms(String evtNo ,EMSLotInfoReportI emsLotInfoReportI){
        EMSLotInfoReportO emsLotInfoReportO = new EMSLotInfoReportO();
        emsLotInfoReportI.setTrxId("reportProcessingData");
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsLotInfoReportI);
        if(!StringUtils.hasText(outTrxStr)){
            emsLotInfoReportO.setRtnCode("00000001");
            emsLotInfoReportO.setRtnMesg("[EAP-MES]:EAP上传生产相关信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsLotInfoReportO.getRtnMesg());
        }else{
            emsLotInfoReportO = JacksonUtils.string2Object(outTrxStr,EMSLotInfoReportO.class);
        }
        return emsLotInfoReportO;
    }
    //上报设备参数信息
    public static EMSDeviceParameterReportO emsDeviceParameterReportToEms(String evtNo, String lotNo,EMSDeviceParameterReportI emsDeviceParameterReportI ){
        EMSDeviceParameterReportO emsDeviceParameterReportO = new EMSDeviceParameterReportO();
        emsDeviceParameterReportI.setTrxId("reportTraceDateEdc");
        emsDeviceParameterReportI.setLotNo(lotNo);
        emsDeviceParameterReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsDeviceParameterReportI);
        if(!StringUtils.hasText(outTrxStr)){
            emsDeviceParameterReportO.setRtnCode("00000001");
            emsDeviceParameterReportO.setRtnMesg("[EAP-MES]:EAP上报设备参数信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsDeviceParameterReportO.getRtnMesg());
        }else{
            emsDeviceParameterReportO = JacksonUtils.string2Object(outTrxStr,EMSDeviceParameterReportO.class);
        }
        return emsDeviceParameterReportO;

    }
    //获取报警定义管控
    public static EMSGetAlarmO getAlarmFromEms(String evtNo,String alarmCode){
        EMSGetAlarmI emsGetAlarmI = new EMSGetAlarmI();
        EMSGetAlarmO emsGetAlarmO = new EMSGetAlarmO();
        emsGetAlarmI.setTrxId("getEqpAlarmControl");
        emsGetAlarmI.setEquipmentType(GenericDataDef.eqpType);
        emsGetAlarmI.setAlarmCode(alarmCode);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsGetAlarmI);
        if(!StringUtils.hasText(outTrxStr)){
            emsGetAlarmO.setRtnCode("00000001");
            emsGetAlarmO.setRtnMesg("[EAP-MES]:EAP获取设备报警定义管控，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsGetAlarmO.getRtnMesg());
        }else{
            emsGetAlarmO = JacksonUtils.string2Object(outTrxStr,EMSGetAlarmO.class);
        }
        return emsGetAlarmO;
    }
    // EAP上传生产Wafer信息
    public static EMSWaferReportO waferInfotoEms(String evtNo,String lotId, String waferId, String waferState){
        EMSWaferReportO emsWaferReportO = new EMSWaferReportO();
        EMSWaferReportI emsWaferReportI = new EMSWaferReportI();
        emsWaferReportI.setTrxId("reportProcessingWaferData");
        emsWaferReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsWaferReportI.setLotNo(lotId);
        emsWaferReportI.setWaferNo(waferId);
        emsWaferReportI.setWaferState(waferState);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsWaferReportI);
        if(!StringUtils.hasText(outTrxStr)){
           emsWaferReportO.setRtnCode("00000001");
           emsWaferReportO.setRtnMesg("[EAP-MES]:EAP上传生产wafer信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsWaferReportO.getRtnMesg());
        }else{
            emsWaferReportO = JacksonUtils.string2Object(outTrxStr,EMSWaferReportO.class);
        }
        return emsWaferReportO;
    }

    //获取测试结果实时卡控规则
    public static EMSGetTestResultO getTestResult(String evtNo,String deviceName){
        EMSGetTestResultI emsGetTestResultI = new EMSGetTestResultI();
        EMSGetTestResultO emsGetTestResultO = new EMSGetTestResultO();
        emsGetTestResultI.setTrxId("getEqpProcessingControl");
        emsGetTestResultI.setDeviceName(deviceName);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, emsGetTestResultI);
        if(!StringUtils.hasText(outTrxStr)){
            emsGetTestResultO.setRtnCode("00000001");
            emsGetTestResultO.setRtnMesg("[EAP-MES]:EAP获取测试结果实时卡控规则，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,emsGetTestResultO.getRtnMesg());
        }else{
             emsGetTestResultO = JacksonUtils.string2Object(outTrxStr,EMSGetTestResultO.class);
        }
        return emsGetTestResultO;
    }
    //上报自动作业信息到EMS
    public static EAPRepRunWorkInfoO reportRunWorkInfo(String evtNo,String title, String lotNo,String alarmImg,String returnState,String message,String methodName){
        EAPRepRunWorkInfoI eapRepRunWorkInfoI = new EAPRepRunWorkInfoI();
        EAPRepRunWorkInfoO eapRepRunWorkInfoO = new EAPRepRunWorkInfoO();
        eapRepRunWorkInfoI.setTrxId("getEqpRunWorkLotLog");
        eapRepRunWorkInfoI.setEquipmentNo(GenericDataDef.equipmentNo);
        eapRepRunWorkInfoI.setLotNo(lotNo);
        eapRepRunWorkInfoI.setTitle(title);
        eapRepRunWorkInfoI.setReturnState(returnState);
        eapRepRunWorkInfoI.setMethod_name(methodName);
        eapRepRunWorkInfoI.setAlarmImg(alarmImg);
        eapRepRunWorkInfoI.setMessage(message);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, emsQueue, emsExchange, eapRepRunWorkInfoI);
        if(!StringUtils.hasText(outTrxStr)){
            eapRepRunWorkInfoO.setRtnCode("00000001");
            eapRepRunWorkInfoO.setRtnMesg("[EAP-MES]:EAP上报自动化步骤详细信息，EMS没有回复");
            ClientHandler.sendMessage(evtNo,false,1,eapRepRunWorkInfoO.getRtnMesg());
        }else{
            eapRepRunWorkInfoO = JacksonUtils.string2Object(outTrxStr,EAPRepRunWorkInfoO.class);
        }
        return eapRepRunWorkInfoO;
    }
    @Autowired
    public  void setRabbitmqHandler(RabbitmqHandler rabbitmqHandler) {
        EmsHandler.rabbitmqHandler = rabbitmqHandler;
    }

    @Value("${spring.rabbitmq.ems.exchange}")
    public  void setEmsExchange(String emsExchange) {
        EmsHandler.emsExchange = emsExchange;
    }
    @Value("${spring.rabbitmq.ems.queue}")
    public  void setEmsQueue(String emsQueue) {
        EmsHandler.emsQueue = emsQueue;
    }
}
