package com.totainfo.eap.cp.tcp.client;


import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.util.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;


@Component
@ChannelHandler.Sharable
public class EchoClientHandler extends ChannelInboundHandlerAdapter {



    @Value("${tcp.client.separator}")
    private String separator;


    @Resource
    private ApplicationContext context;


    private Map<String, String> waferInfoMap = new ConcurrentHashMap<>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg == null){
            return;
        }
        String message = msg.toString();
        if(StringUtils.isEmpty(message)){
            return;
        }
        message = message.replaceAll("\\r","").replaceAll("\\n", "");
        LogUtils.info("GPIB->EAP:["+ message + "]");
        String[] strs = message.split("\"");
        String evtNo = GUIDGenerator.javaGUID();
        EapBaseService eapBaseService;
         for(String item:strs){
             if(item.startsWith("RECEIVED")){
                 continue;
             }
             if(item.startsWith("sl") && item.length() > 2){ //DeviceName
                 GPIBDeviceNameReportI gpibDeviceNameReportI = new GPIBDeviceNameReportI();
                 gpibDeviceNameReportI.setDeviceName(item.substring(2).trim());
                 eapBaseService = (EapBaseService) context.getBean("deviceNameReport");
                 eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibDeviceNameReportI));
             }else if(item.startsWith("E") && item.length() > 2){  //AlarmCode
                 String alarmCode = item.substring(1).trim();
                 String key = String.format("EQPT:%s:ALARMCODE", equipmentNo);
                 AsyncUtils.setResponse(key, alarmCode);
             }else if(item.startsWith("e") && item.length() > 2){  //AlarmMessage
                 String alarmMesg = item.substring(1).trim();
                 String key = String.format("EQPT:%s:ALARMMESG", equipmentNo);
                 AsyncUtils.setResponse(key, alarmMesg);
             } else if(item.startsWith("V") && item.length() > 2){
                 String logNo = item.substring(1).trim();
                 GPIBLotStartReportI gpibLotStartReportI = new GPIBLotStartReportI();
                 gpibLotStartReportI.setLotNo(logNo);
                 eapBaseService = (EapBaseService) context.getBean("lotStartReport");
                 eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibLotStartReportI));
             }else if(item.startsWith("ku") && item.length() >2){
                 String siteNum = item.substring(11,12);
                 String notchDirection = item.substring(15,18);
                 waferInfoMap.put("siteNum", siteNum);
                 waferInfoMap.put("notchDirection", notchDirection);
             }else if(item.startsWith("b") && item.length() >2){
                 String prWaferId = waferInfoMap.get("waferId");
                 String curWaferId = item.substring(1).trim();
                 waferInfoMap.put("waferId", curWaferId);
                 GPIBWaferStartReportI gpibWaferStartReportI = new GPIBWaferStartReportI();
                 gpibWaferStartReportI.setWaferId(curWaferId);
                 gpibWaferStartReportI.setPvWaferId(prWaferId);
                 eapBaseService = (EapBaseService) context.getBean("waferStartReport");
                 eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibWaferStartReportI));

             }else if(item.startsWith("O") && item.length() >=2){

             }else if(item.startsWith("Q") && item.length() >=2){
                 String startCoordinate = item.substring(1).trim();
                 waferInfoMap.put("startCoorDinate", startCoordinate);
             }else if(item.startsWith("M") && item.length() >=2){
                 String result = item.substring(1).trim();
                 waferInfoMap.put("result", result);
                 eapBaseService = (EapBaseService) context.getBean("dieInfoReport");
                 eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(waferInfoMap));
             }else if(item.startsWith("^") && item.length() ==1){
                 GPIBLotEndReportI endReportI = new GPIBLotEndReportI();
                 eapBaseService = (EapBaseService) context.getBean("lotEndReport");
                 eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(endReportI));
             }
         }
    }

}
