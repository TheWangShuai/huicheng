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
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 13:36
 */
@Component
public class ClientHandler {


    private static  RabbitmqHandler rabbitmqHandler;

    private static String clientQueue;
    private static  String clientExchange;

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
