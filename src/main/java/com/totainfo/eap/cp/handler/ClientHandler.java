package com.totainfo.eap.cp.handler;

import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.IEapBaseInterface;
import com.totainfo.eap.cp.trx.client.EAPMessageSend.EAPMessageSendI;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
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
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody.RmsQueryRecipeBodyI;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody.RmsQueryRecipeBodyO;
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

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.CLIENT_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.MES_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:36
 */
@Component
public class ClientHandler {


    private static  RabbitmqHandler rabbitmqHandler;

    private static final String appName = "CLIENT";
    private static String clientQueue;
    private static  String clientExchange;



    public static void sendEqpInfo(String evtNo, EAPSyncEqpInfoI eapSyncEqpInfoI){
        rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapSyncEqpInfoI);
    }

    public static RmsQueryRecipeBodyO sendRecipeInfo(String evtNo, String recipeId,String toolType){
        RmsQueryRecipeBodyI rmsQueryRecipeBodyI = new RmsQueryRecipeBodyI();
        RmsQueryRecipeBodyO rmsQueryRecipeBodyO = new RmsQueryRecipeBodyO();
        rmsQueryRecipeBodyI.setTrxId("未定");
        rmsQueryRecipeBodyI.setActionFlg("未定");
        rmsQueryRecipeBodyI.setRecipeId(recipeId);
        rmsQueryRecipeBodyI.setToolType(toolType);
        String outTrxStr = rabbitmqHandler.sendForReply(evtNo, appName, clientExchange, clientQueue, rmsQueryRecipeBodyI);
        if (!StringUtils.hasText(outTrxStr)) {
            rmsQueryRecipeBodyO.setRtnCode(CLIENT_TIME_OUT);
            rmsQueryRecipeBodyO.setRtnMesg("[EAP-Client]:EAP请求配方:[" + recipeId + "]的recipeBody，Client没有回复");
            ClientHandler.sendMessage(evtNo,false,1,rmsQueryRecipeBodyO.getRtnMesg());
        } else {
            rmsQueryRecipeBodyO = JacksonUtils.string2Object(outTrxStr, RmsQueryRecipeBodyO.class);
        }
        return rmsQueryRecipeBodyO;
    }

    public static void sendMessage(String evtNo, boolean isPopUp, int messageType, String message){
        EAPMessageSendI eapMessageSendI = new EAPMessageSendI();
        eapMessageSendI.setTrxId("SynMessage");
        eapMessageSendI.setTrypeId("I");
        eapMessageSendI.setActionFlg("RLC");
        eapMessageSendI.setIspopUp(isPopUp);
        eapMessageSendI.setMessageType(messageType);
        eapMessageSendI.setMessage(message);
        rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapMessageSendI);
    }



    @Autowired
    public void setRabbitmqHandler(RabbitmqHandler rabbitmqHandler) {
        ClientHandler.rabbitmqHandler = rabbitmqHandler;
    }

    @Value("${spring.rabbitmq.client.queue}")
    public void setMesQueue(String queue) {
        ClientHandler.clientQueue = queue;
    }

    @Value("${spring.rabbitmq.client.exchange}")
    public void setMesExchange(String exchange) {
        ClientHandler.clientExchange = exchange;
    }

}
