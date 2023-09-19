package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationI;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationIA;
import com.totainfo.eap.cp.trx.rms.RmsOnlineValidation.RmsOnlineValidationO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author xiaobin.Guo
 * @date 2022年11月18日 11:24
 */
@Component
public class RMSHandler {


    private static  RabbitmqHandler rabbitmqHandler;

    private static final String appName = "RMS";

    private static String rmsExchange;

    private static String rmsQueue;

    private static String rmsToken;

    private static int rmsTimeout;


    public static RmsOnlineValidationO toRmsOnlineValidation(String evtNo, String eqptId, String lotId, String recipeId) {


        RmsOnlineValidationIA rmsOnlineValidationIA = new RmsOnlineValidationIA();
        rmsOnlineValidationIA.setToolId(eqptId);
        rmsOnlineValidationIA.setLotId(lotId);
        rmsOnlineValidationIA.setRecipeId(recipeId);

        List<RmsOnlineValidationIA> rmsOnlineValidationIAList = new ArrayList<>(1);
        rmsOnlineValidationIAList.add(rmsOnlineValidationIA);

        RmsOnlineValidationI rmsOnlineValidationI = new RmsOnlineValidationI();
        rmsOnlineValidationI.setTrxId("onlineValidation");
        rmsOnlineValidationI.setTrxId("I");
        rmsOnlineValidationI.setJobId(evtNo);
        rmsOnlineValidationI.setBisRecipeVOList(rmsOnlineValidationIAList);

        String returnMsg = rabbitmqHandler.sendForReply(evtNo, appName, rmsExchange, rmsQueue, rmsOnlineValidationI);
        if (!StringUtils.hasText(returnMsg)) {
            return null;
        }
        return JacksonUtils.string2Object(returnMsg, RmsOnlineValidationO.class);
    }


    @Value("${spring.rabbitmq.rms.queue}")
    public void setRmsQueue(String rmsQueue) {
        RMSHandler.rmsQueue = rmsQueue;
    }

    @Value("${spring.rabbitmq.rms.exchange}")
    public void setRmsExchange(String rmsExchange) {
        RMSHandler.rmsExchange = rmsExchange;
    }


    @Autowired
    public void setRabbitmqHandler(RabbitmqHandler rabbitmqHandler) {
        RMSHandler.rabbitmqHandler = rabbitmqHandler;
    }
}
