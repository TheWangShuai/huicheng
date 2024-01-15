package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.tcp.client.EchoClient;
import com.totainfo.eap.cp.tcp.client.EchoClientHandler;
import com.totainfo.eap.cp.util.AsyncUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:44
 */
@Component
public class GPIBHandler {

    private static  EchoClient echoClient;

    public static void changeMode(String modeCmmd){
        echoClient.send("GPIB", modeCmmd);
    }

    public static void getDeviceName(){
        changeMode("++master");
        echoClient.send("GPIB", "sl");
    }

    public static String getAlarmCode(){
        changeMode("++master");
        String key = String.format("EQPT:%s:ALARMCODE", equipmentNo);
        AsyncUtils.setRequest(key, 30000);
        echoClient.send("GPIB", "E");
        String alarmCode = AsyncUtils.getResponse(key, 30000);
        changeMode("++device");
        return alarmCode;
    }

    public static String getAlarmMessage(){
        changeMode("++master");
        String key = String.format("EQPT:%s:ALARMMESG", equipmentNo);
        AsyncUtils.setRequest(key, 30000);
        echoClient.send("GPIB", "e");
        String alarmMesg = AsyncUtils.getResponse(key, 30000);
        changeMode("++device");
        return alarmMesg;
    }

    @Autowired
    public void setEchoClient(EchoClient echoClient) {
        GPIBHandler.echoClient = echoClient;
    }
}