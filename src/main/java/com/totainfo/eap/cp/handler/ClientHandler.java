package com.totainfo.eap.cp.handler;

import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.IEapBaseInterface;
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
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:36
 */
@Component
public class ClientHandler {


    private static RabbitTemplate clientRabbitTemplate;

    private static AsyncRabbitTemplate clientAsyncRabbitTemplate;

    private static int timeout;
    private static String clientQueue;
    private static  String clientExchange;

    private static String computerName;
    private static String equipmentNo;
    static {
        InetAddress address = null; // 此处可以是计算机名或者IP，任一即可
        try {
            address = InetAddress.getByName("HostNameOrIP");
        } catch (UnknownHostException e) {
            LogUtils.error("获取服务器名失败", e);
        }
        computerName = address.getHostName();
    }



    public static EAPEqptAlarmReportO alarmReport(String alarmCode, String alarmText, String time){
        EAPEqptAlarmReportI eapEqptAlarmReportI = new EAPEqptAlarmReportI();
        EAPEqptAlarmReportO eapEqptAlarmReportO = new EAPEqptAlarmReportO();
        eapEqptAlarmReportI.setEvtUsr("");
        eapEqptAlarmReportI.setComputerName(computerName);
        eapEqptAlarmReportI.setEquipmentNo(equipmentNo);
        eapEqptAlarmReportI.setErrorcode(alarmCode);
        eapEqptAlarmReportI.setMessage(alarmText);
        eapEqptAlarmReportI.setTime(time);

        String inTrxStr = JacksonUtils.object2String(eapEqptAlarmReportI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptAlarmReportO.setRtnCode("00000001");
            eapEqptAlarmReportO.setRtnMesg("EAP发送设备报警，MES没有回复");
        }else{
            eapEqptAlarmReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptAlarmReportO.class);
        }
        return eapEqptAlarmReportO;
    }

    public static EAPEqptStatusReportO eqptStatReport(String eqptStat, String remark){
        EAPEqptStatusReportI eapEqptStatusReportI = new EAPEqptStatusReportI();
        EAPEqptStatusReportO eapEqptStatusReportO = new EAPEqptStatusReportO();
        eapEqptStatusReportI.setEvtUsr("");
        eapEqptStatusReportI.setComputerName(computerName);
        eapEqptStatusReportI.setEquipmentNo(equipmentNo);
        eapEqptStatusReportI.setLastState(eqptStat);
        eapEqptStatusReportI.setRemark(remark);

        String inTrxStr = JacksonUtils.object2String(eapEqptStatusReportI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapEqptStatusReportO.setRtnCode("00000001");
            eapEqptStatusReportO.setRtnMesg("EAP发送设备状态，MES没有回复");
        }else{
            eapEqptStatusReportO = JacksonUtils.string2Object(outTrxStr, EAPEqptStatusReportO.class);
        }
        return eapEqptStatusReportO;
    }

    public static EAPReqLotInfoO lotInfoReq(String lotNo){
        EAPReqLotInfoI eapReqLotInfoI = new EAPReqLotInfoI();
        EAPReqLotInfoO eapReqLotInfoO = new EAPReqLotInfoO();
        eapReqLotInfoI.setEvtUsr("");
        eapReqLotInfoI.setLotNo(lotNo);
        eapReqLotInfoI.setComputerName(computerName);
        eapReqLotInfoI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqLotInfoI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqLotInfoO.setRtnCode("00000001");
            eapReqLotInfoO.setRtnMesg("EAP发送设备状态，MES没有回复");
        }else{
            eapReqLotInfoO = JacksonUtils.string2Object(outTrxStr, EAPReqLotInfoO.class);
        }
        return eapReqLotInfoO;
    }


    public static EAPReqCheckInO checkInReq(String lotNo){
        EAPReqCheckInI eapReqCheckInI = new EAPReqCheckInI();
        EAPReqCheckInO eapReqCheckInO = new EAPReqCheckInO();
        eapReqCheckInI.setEvtUsr("");
        eapReqCheckInI.setLotNo(lotNo);
        eapReqCheckInI.setComputerName(computerName);
        eapReqCheckInI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqCheckInI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckInO.setRtnCode("00000001");
            eapReqCheckInO.setRtnMesg("EAP发送Lot:["+lotNo+"] Check In，MES没有回复");
        }else{
            eapReqCheckInO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckInO.class);
        }
        return eapReqCheckInO;
    }



    public static EAPReqCheckOutO checkOutReq(String lotNo){
        EAPReqCheckOutI eapReqCheckOutI = new EAPReqCheckOutI();
        EAPReqCheckOutO eapReqCheckOutO = new EAPReqCheckOutO();
        eapReqCheckOutI.setEvtUsr("");
        eapReqCheckOutI.setLotNo(lotNo);
        eapReqCheckOutI.setComputerName(computerName);
        eapReqCheckOutI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqCheckOutI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqCheckOutO.setRtnCode("00000001");
            eapReqCheckOutO.setRtnMesg("EAP发送Lot:["+lotNo+"] Check In，MES没有回复");
        }else{
            eapReqCheckOutO = JacksonUtils.string2Object(outTrxStr, EAPReqCheckOutO.class);
        }
        return eapReqCheckOutO;
    }


    public static EAPReqMeasureResultO measureResultReq(String lotNo){
        EAPReqMeasureResultI eapReqMeasureResultI = new EAPReqMeasureResultI();
        EAPReqMeasureResultO eapReqMeasureResultO = new EAPReqMeasureResultO();
        eapReqMeasureResultI.setEvtUsr("");
        eapReqMeasureResultI.setLotNo(lotNo);
        eapReqMeasureResultI.setComputerName(computerName);
        eapReqMeasureResultI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapReqMeasureResultI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapReqMeasureResultO.setRtnCode("00000001");
            eapReqMeasureResultO.setRtnMesg("EAP请求Lot:["+lotNo+"] 量测结果，MES没有回复");
        }else{
            eapReqMeasureResultO = JacksonUtils.string2Object(outTrxStr, EAPReqMeasureResultO.class);
        }
        return eapReqMeasureResultO;
    }


    public static EAPUploadDieResultO uploadDieResult(String lotNo, String waferId, String startCoordinates, String result){
        EAPUploadDieResultI eapUploadDieResultI = new EAPUploadDieResultI();
        EAPUploadDieResultO eapUploadDieResultO = new EAPUploadDieResultO();
        eapUploadDieResultI.setEvtUsr("");
        eapUploadDieResultI.setLotNo(lotNo);
        eapUploadDieResultI.setWaferId(waferId);
        eapUploadDieResultI.setStartingCoordinates(startCoordinates);
        eapUploadDieResultI.setResult(result);
        eapUploadDieResultI.setComputerName(computerName);
        eapUploadDieResultI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapUploadDieResultI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapUploadDieResultO.setRtnCode("00000001");
            eapUploadDieResultO.setRtnMesg("EAP上传Lot:["+lotNo+"],Wafer:["+waferId+"],DIE 测试结果，MES没有回复");
        }else{
            eapUploadDieResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadDieResultO.class);
        }
        return eapUploadDieResultO;
    }

    public static EAPUploadMarkResultO uploadMarkResult(String lotNo, String waferId, String startCoordinates, String result, String remark){
        EAPUploadMarkResultI eapUploadMarkResultI = new EAPUploadMarkResultI();
        EAPUploadMarkResultO eapUploadMarkResultO = new EAPUploadMarkResultO();
        eapUploadMarkResultI.setEvtUsr("");
       eapUploadMarkResultI.setLotNo(lotNo);
       eapUploadMarkResultI.setWaferId(waferId);
       eapUploadMarkResultI.setStartingCoordinates(startCoordinates);
       eapUploadMarkResultI.setResult(result);
       eapUploadMarkResultI.setRemark(remark);
       eapUploadMarkResultI.setComputerName(computerName);
       eapUploadMarkResultI.setEquipmentNo(equipmentNo);

        String inTrxStr = JacksonUtils.object2String(eapUploadMarkResultI);
        String outTrxStr = sendMessageToMes("", inTrxStr);
        if(!StringUtils.hasText(outTrxStr)){
            eapUploadMarkResultO.setRtnCode("00000001");
            eapUploadMarkResultO.setRtnMesg("EAP上传Lot:["+lotNo+"],Wafer:["+waferId+"] 针痕检测结果，MES没有回复");
        }else{
            eapUploadMarkResultO = JacksonUtils.string2Object(outTrxStr, EAPUploadMarkResultO.class);
        }
        return eapUploadMarkResultO;
    }

    private static String sendMessageToMes(String trxId, String inObjStr){
        MessageProperties properties = new MessageProperties();
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setAppId("EAP");
        properties.setExpiration(String.valueOf(timeout));
        properties.getHeaders().put("trxId", trxId);
        Message message = new Message(inObjStr.getBytes(), properties);
        LogUtils.info("[{}][{}]:[{}]", trxId, "EAP->GUI", inObjStr);
        AsyncRabbitTemplate.RabbitMessageFuture future = clientAsyncRabbitTemplate.sendAndReceive(clientExchange, clientExchange, message);
        String reply = _SPACE;
        Message rtnMessage = null;
        try {
            rtnMessage = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LogUtils.error("RabbitMQ Receive Exception", e);
        }
        if (rtnMessage != null) {
            reply = new String(rtnMessage.getBody());
        }
        LogUtils.info("[{}][{}]:[{}]", trxId, "GUI->EAP", reply);
        return reply;
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue("${spring.rabbitmq.client.serverMq}"), exchange = @Exchange("${spring.rabbitmq.client.serverExchange}"), key = "${spring.rabbitmq.eap.serverMq}"))
     private void lisenterMq(Message msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) {
        MessageProperties properties = msg.getMessageProperties();
        try {
            String trxId = _SPACE;
            String evtNo = properties.getMessageId();
            String appId = properties.getAppId();
            Object trxObj = properties.getHeaders().get("trxId");
            String message = new String(msg.getBody());

            String replyQueue = properties.getReplyTo();
            LogUtils.info("[{}][{}]:[{}]",  "GUI->EAP",trxId, message);
            IEapBaseInterface commService = (IEapBaseInterface) MatrixAppContext.getBean(trxId);
            String rtnMesg = commService.subMainProc(evtNo, message);
            if (StringUtils.hasText(replyQueue)) {
                clientRabbitTemplate.send(replyQueue, new Message(rtnMesg.getBytes(), properties));
                LogUtils.info("[{}][{}]:[{}]",  "EAP->GUI", trxId, rtnMesg);
            }
        } catch (Exception e) {
            LogUtils.error("Service Exception：", e);
        }finally {
            //直接ack
            try {
                channel.basicAck(deliverTag, true);
            } catch (IOException e) {
                LogUtils.error("ACK 异常", e);
            }
        }
    }

    @Autowired
    public void setMesRabbitTemplate(RabbitTemplate clientRabbitTemplate) {
        ClientHandler.clientRabbitTemplate = clientRabbitTemplate;
    }

    @Autowired
    public void setMesAsyncRabbitTemplate(AsyncRabbitTemplate clientAsyncRabbitTemplate) {
        ClientHandler.clientAsyncRabbitTemplate = clientAsyncRabbitTemplate;
    }

    @Value("${equipment.no}")
    public void setEquipmentNo(String equipmentNo) {
        ClientHandler.equipmentNo = equipmentNo;
    }

    @Value("${spring.rabbitmq.client.timeout}")
    public  void setTimeout(int timeout) {
        ClientHandler.timeout = timeout;
    }

    @Value("${spring.rabbitmq.client.clientMq}")
    public void setMesQueue(String mesQueue) {
        ClientHandler.clientQueue = mesQueue;
    }

    @Value("${spring.rabbitmq.client.clientExchange}")
    public void setMesExchange(String clientExchange) {
        ClientHandler.clientExchange = clientExchange;
    }

}
