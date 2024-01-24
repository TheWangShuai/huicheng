package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.tcp.client.EchoClient;
import com.totainfo.eap.cp.tcp.client.EchoClientHandler;
import com.totainfo.eap.cp.tcp.server.EchoServerHandler;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:44
 */
@Component
public class GPIBHandler {
    @Value("${GPIB.address}")
    private static String address;

    private static EchoServerHandler echoServerHandler;


    public static void changeMode(String modeCmmd) {
        if (modeCmmd.equals("++master")) {
            echoServerHandler.send("GPIB", modeCmmd);
            String format ="++addr 1";
            LogUtils.info("format是[{}]",format);
            echoServerHandler.send("GPIB", format);
        } else {
            echoServerHandler.send("GPIB", modeCmmd);
        }

    }

    public static void getDeviceName() {
        changeMode("++master");
        echoServerHandler.send("GPIB", "sl");
    }

    public static String getAlarmCode() {
        changeMode("++master");
        String key = String.format("EQPT:%s:ALARMCODE", equipmentNo);
        AsyncUtils.setRequest(key, 30000);
        echoServerHandler.send("GPIB", "E");
        String alarmCode = AsyncUtils.getResponse(key, 30000);
        changeMode("++device");
        return alarmCode;
    }

    public static String getAlarmMessage() {
        changeMode("++master");
        String key = String.format("EQPT:%s:ALARMMESG", equipmentNo);
        AsyncUtils.setRequest(key, 30000);
        echoServerHandler.send("GPIB", "e");
        String alarmMesg = AsyncUtils.getResponse(key, 30000);
        changeMode("++device");
        return alarmMesg;
    }


    @Autowired
    public void setEchoServerHandler(EchoServerHandler echoServerHandler) {
        GPIBHandler.echoServerHandler = echoServerHandler;
    }
}
