package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.commdef.GenergicCodeDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RcmHandler {

    private static RabbitmqHandler rabbitmqHandler;
    private static final String appName = "RCM";

    private static String rcmQueue;
    private static  String rcmExchange;

    public static EapReportInfoO lotInfoReport(String evtNo, EapReportInfoI eapReportInfoI){
        EapReportInfoO eapReportInfoO = new EapReportInfoO();
        eapReportInfoI.setTrxId("eapReportInfo");
        eapReportInfoI.setEquipmentNo(GenericDataDef.equipmentNo);
        String reply = rabbitmqHandler.sendForReply(evtNo, appName, rcmQueue, rcmExchange, eapReportInfoI);
        if(!StringUtils.hasText(reply)){
            eapReportInfoO.setRtnCode(GenergicCodeDef.RCM_TIME_OUT);
            eapReportInfoO.setRtnMesg("[EAP->RCM]:EAP上报设备信息，RCM没有回复");
            ClientHandler.sendMessage(evtNo,false,1, eapReportInfoO.getRtnMesg());
        }else {
            eapReportInfoO = JacksonUtils.string2Object(reply, EapReportInfoO.class);
        }
        return eapReportInfoO;

    }



    @Autowired
    public void setRabbitmqHandler(RabbitmqHandler rabbitmqHandler){
        RcmHandler.rabbitmqHandler = rabbitmqHandler;
    }
    @Value("${spring.rabbitmq.rcm.queue}")
    public void setRcmQueue(String queue){
        RcmHandler.rcmQueue = queue;
    }
    @Value("${spring.rabbitmq.rcm.exchange}")
    public void setRcmExchange(String exchange){
        RcmHandler.rcmExchange =exchange;
    }

}
