package com.totainfo.eap.cp.handler;

import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.IEapBaseInterface;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelI;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelO;
import com.totainfo.eap.cp.trx.client.EAPMessageSend.EAPMessageSendI;
import com.totainfo.eap.cp.trx.client.EAPMessageSendK.EAPMessageSentKO;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelI;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.client.EAPUserPermission.EAPUserPermissionO;
import com.totainfo.eap.cp.trx.client.EAPWaferLocation.EAPWaferLocationI;
import com.totainfo.eap.cp.trx.client.EAPWaferLocation.EAPWaferLocationO;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportI;
import com.totainfo.eap.cp.trx.mes.EAPEqptAlarmReport.EAPEqptAlarmReportO;
import com.totainfo.eap.cp.trx.mes.EAPEqptStatusReport.EAPEqptStatusReportI;
import com.totainfo.eap.cp.trx.mes.EAPEqptStatusReport.EAPEqptStatusReportO;
import com.totainfo.eap.cp.trx.mes.EAPLotInfoBase.EAPLotInfoBaseO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckOut.EAPReqCheckOutO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfo0D;
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
import com.totainfo.eap.cp.util.GUIDGenerator;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
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

    @Resource
    private  StateDao stateDao;

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
        EAPReqLotInfo0D eapReqLotInfo0D;
        eapMessageSendI.setTrxId("SynMessage");
        eapMessageSendI.setTrypeId("I");
        eapMessageSendI.setActionFlg("RLC");
        eapMessageSendI.setIspopUp(isPopUp);
        eapMessageSendI.setMessageType(messageType);
        if (message.contains("5000000") && message.contains("null")){
            eapReqLotInfo0D = JacksonUtils.string2Object(message, EAPReqLotInfo0D.class);
            eapMessageSendI.setMessage(eapReqLotInfo0D.getRtnMesg());
            rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapMessageSendI);
        }else {
            eapMessageSendI.setMessage(message);
            rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapMessageSendI);
        }
    }
    // 权限校验MES接口
    public static void sendUserHandk(String evtNo, EAPUserPermissionO eapUserPermissionO){
        EAPMessageSentKO eapMessageSentKO = new EAPMessageSentKO();
        eapMessageSentKO.setTrxId(eapUserPermissionO.getTrxId());
        eapMessageSentKO.setActionFlg(eapUserPermissionO.getActionFlg());
        eapMessageSentKO.setRtnCode(eapUserPermissionO.getRtnCode());
        eapMessageSentKO.setRtnMessage(eapUserPermissionO.getRtnMesg());
        rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapMessageSentKO);
    }
    //上报WaferSlot信息给Client
    public static void waferSlotReport(String evtNo,String rtnCode,String rtnMesg, String sampleValue){
        EAPWaferLocationI eapWaferLocationI = new EAPWaferLocationI();
        eapWaferLocationI.setTrxId("GETLOTINFO");
        eapWaferLocationI.setActionFlg("SOLTMAP");
        eapWaferLocationI.setSampleValue(sampleValue);
        eapWaferLocationI.setRtnCode(rtnCode);
        eapWaferLocationI.setRtnMesg(rtnMesg);
        rabbitmqHandler.send (evtNo,appName,clientExchange, clientQueue, eapWaferLocationI);
    }
    // 给Client端上报GPIB状态
    public static void sendGPIBState(String evtNo, EAPRepCurModelO eapRepCurModelO){
        EAPRepCurModelI eapRepCurModelI = new EAPRepCurModelI();
        eapRepCurModelI.setTrxId("PUSHGPIBSTATE");
        eapRepCurModelI.setActionFlg("GPIBSTATE");
        eapRepCurModelI.setRtnCode(eapRepCurModelO.getRtnCode());
        eapRepCurModelI.setRtnMesg(eapRepCurModelO.getRtnMesg());
        eapRepCurModelI.setState(eapRepCurModelO.getState());
        rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapRepCurModelI);
    }

    // 切换GPIB为从机模式
    public static void changeGPIBMode(String evtNo,EAPChangeGPIBModelO eapChangeGPIBModelO){
        EAPChangeGPIBModelI eapChangeGPIBModelI   = new EAPChangeGPIBModelI();
        eapChangeGPIBModelI.setTrxId("ChangeGPIBState");
        eapChangeGPIBModelI.setActionFlg("GPIBSLAVEMODEL");
        eapChangeGPIBModelI.setRtnCode(eapChangeGPIBModelO.getRtnCode());
        eapChangeGPIBModelI.setRtnMesg(eapChangeGPIBModelO.getRtnCode());
        rabbitmqHandler.send(evtNo, appName, clientExchange,clientQueue, eapChangeGPIBModelI);
    }

    public void setFlowStep(String step, String stepSate){
        String evtNo = UUID.randomUUID().toString();
        StateInfo stateInfo = stateDao.getStateInfo();
        LogUtils.info("Redis中存在的步骤为: " + stateInfo.getStep() + ", 步骤状态为: " + stateInfo.getState() );
        if (stateInfo == null){
            ClientHandler.sendMessage(evtNo,false,2,"设备的步骤信息在Redis中不存在!");
            return;
        }
        if (Integer.parseInt(step) >= Integer.parseInt(stateInfo.getStep())){
            stateInfo.setStep(step);
            stateInfo.setState(stepSate);
            stateDao.addStateInfo(stateInfo);
        }
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
