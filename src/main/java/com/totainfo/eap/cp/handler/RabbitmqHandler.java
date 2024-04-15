package com.totainfo.eap.cp.handler;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.util.*;
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
import java.util.concurrent.TimeUnit;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;

@Component
public class RabbitmqHandler<I extends BaseTrxI> {

    @Value("${spring.rabbitmq.timeout}")
    private int timeout;



    private RabbitTemplate rabbitTemplate;


    public AsyncRabbitTemplate asyncRabbitTemplate;


    public void send(String evtNo, String appName, String exchange, String queue, I inObj) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(evtNo);
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setAppId("EAP");
        properties.setExpiration(String.valueOf(timeout));
        properties.getHeaders().put("trxId", inObj.getTrxId());
        properties.getHeaders().put("actionFlag", inObj.getActionFlg());
        String inObjStr = JacksonUtils.object2String(inObj);
        Message message = new Message(inObjStr.getBytes(), properties);

        rabbitTemplate.convertAndSend(exchange, queue, message);
        LogUtils.info("[{}][{}][{}][{}][{}]:[{}]", evtNo,"EAP->"+appName,"Exchange:"+exchange,"Queue:"+queue,inObj.getTrxId(),  inObjStr);
    }

    public String sendForReply(String evtNo, String appName, String queue, String exchange, I inObj) {
        String trxId = inObj.getTrxId();
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(evtNo);
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setAppId("EAP");
        properties.setExpiration(String.valueOf(timeout));
        properties.getHeaders().put("trxId", inObj.getTrxId());
        properties.getHeaders().put("actionFlag", inObj.getActionFlg());
        String inObjStr = JacksonUtils.object2String(inObj);
        Message message = new Message(inObjStr.getBytes(), properties);
        LogUtils.info("向EMS的Rabbitmq中发送的消息为: " + inObjStr);
        LogUtils.info("[{}][{}][{}][{}][{}]:[{}]", evtNo,"EAP->"+appName,"Exchange:"+exchange,"Queue:"+queue,inObj.getTrxId(),  message);
        String reply = _SPACE;
        Message rtnMessage = null;
        try {
        AsyncRabbitTemplate.RabbitMessageFuture future = asyncRabbitTemplate.sendAndReceive(exchange, queue, message);
            rtnMessage = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LogUtils.error("RabbitMQ Receive Exception", e);
        }
        if (rtnMessage != null) {
            reply = new String(rtnMessage.getBody());
        }
        LogUtils.info("[{}][{}]:[{}][{}]",  evtNo,appName+"->EAP",trxId,  reply);
        return reply;
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue("${spring.rabbitmq.eap.queue}"), exchange = @Exchange("${spring.rabbitmq.eap.exchange}"), key = "${spring.rabbitmq.eap.queue}"))
    public void lisenterMq(Message msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) {
        MessageProperties properties = msg.getMessageProperties();
        try {
            String trxId = _SPACE;
            String appId = properties.getAppId();
            Object trxObj = properties.getHeaders().get("trxId");
            String message = new String(msg.getBody());
            ObjectNode jsonObject = JacksonUtils.getJson2(message);
            String evtNo = GUIDGenerator.javaGUID();
            if(trxObj == null){
                trxId = jsonObject.get("trxId").textValue();
            }else{
                trxId = trxObj.toString();
            }

            String replyQueue = properties.getReplyTo();
            if (!StringUtils.hasText(trxId)) {
                return;
            }
            LogUtils.info("[{}][{}]:[{}][{}]", evtNo, appId+"->EAP",  trxId,  message);


            if(jsonObject.has("jobId")){
                String jobId = jsonObject.get("jobId").textValue();
                AsyncUtils.setResponse(jobId, message);
                return;
            }

            EapBaseService commService = (EapBaseService) MatrixAppContext.getBean(trxId);
            String rtnMesg = commService.subMainProc(evtNo, message);
            if (StringUtils.hasText(replyQueue)) {
                rabbitTemplate.send(replyQueue, new Message(rtnMesg.getBytes(), properties));
                LogUtils.info("[{}][{}]:[{}][{}]", evtNo,  "EAP->"+ appId,trxId, rtnMesg);
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
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setAsyncRabbitTemplate(AsyncRabbitTemplate asyncRabbitTemplate) {
        this.asyncRabbitTemplate = asyncRabbitTemplate;
    }
}
