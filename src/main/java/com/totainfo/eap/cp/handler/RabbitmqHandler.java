package com.totainfo.eap.cp.handler;


import com.rabbitmq.client.Channel;
import com.totainfo.eap.cp.base.service.IEapBaseInterface;
import com.totainfo.eap.cp.base.trx.BaseTrxI;
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
import java.util.concurrent.TimeUnit;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;

@Component
public class RabbitmqHandler<I extends BaseTrxI> {

    @Value("${spring.rabbitmq.rms.timeout}")
    private int rmsTimeout;

    @Value("${spring.rabbitmq.eap.queue}")
    private String eapQueue;

    @Value("${spring.rabbitmq.eap.exchange}")
    private String eapExchange;



    private RabbitTemplate rabbitTemplate;


    public AsyncRabbitTemplate asyncRabbitTemplate;




    public void send(String evtNo, String exchange, String queue, I inObj) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(evtNo);
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setAppId("EAP");
        properties.setExpiration(String.valueOf(rmsTimeout));
        properties.getHeaders().put("trxId", inObj.getTrxId());
        String inObjStr = JacksonUtils.object2String(inObj);
        Message message = new Message(inObjStr.getBytes(), properties);

        LogUtils.info("[{}][{}]:[{}]", inObj.getTrxId(), "EAP->RMS", inObjStr);
        rabbitTemplate.convertAndSend(exchange, queue, message);
    }

    public String sendForReply(String evtNo, String token, String exchange, String queue, int timeout, I inObj) {

        String trxId = inObj.getTrxId();
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(evtNo);
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setAppId("EAP");
        properties.setExpiration(String.valueOf(rmsTimeout));
        properties.getHeaders().put("token", token);
        properties.getHeaders().put("trxId", inObj.getTrxId());
        String inObjStr = JacksonUtils.object2String(inObj);
        Message message = new Message(inObjStr.getBytes(), properties);
        LogUtils.info("[{}][{}]:[{}]", trxId, "EAP->RMS", inObjStr);
        AsyncRabbitTemplate.RabbitMessageFuture future = asyncRabbitTemplate.sendAndReceive(exchange, queue, message);
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
        LogUtils.info("[{}][{}]:[{}]", trxId, "RMS->EAP", reply);
        return reply;
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue("${spring.rabbitmq.eap.queue}"), exchange = @Exchange("${spring.rabbitmq.eap.exchange}"), key = "${spring.rabbitmq.eap.queue}"))
    public void lisenterMq(Message msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) {
        MessageProperties properties = msg.getMessageProperties();
        try {
            String trxId = _SPACE;
            String evtNo = properties.getMessageId();
            String appId = properties.getAppId();
            Object trxObj = properties.getHeaders().get("trxId");
            String message = new String(msg.getBody());

            String replyQueue = properties.getReplyTo();
            if (!StringUtils.hasText(appId) || !StringUtils.hasText(trxId)) {
                return;
            }
            if ("MES".equals(appId)) {
                LogUtils.info("[{}][{}]:[{}]", trxId, "MES->EAP", message);
            } else if ("RMS".equals(appId)) {
                LogUtils.info("[{}][{}]:[{}]", trxId, "RMS->EAP", message);
            }

            IEapBaseInterface commService = (IEapBaseInterface) MatrixAppContext.getBean(trxId);
            String rtnMesg = commService.subMainProc(evtNo, message);
            if (StringUtils.hasText(replyQueue)) {
                rabbitTemplate.send(replyQueue, new Message(rtnMesg.getBytes(), properties));
                if ("MES".equals(appId)) {
                    LogUtils.info("[{}][{}]:[{}]", trxId, "EAP->MES", rtnMesg);
                } else if ("RMS".equals(appId)) {
                    LogUtils.info("[{}][{}]:[{}]", trxId, "EAP->RMS", rtnMesg);
                }
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
