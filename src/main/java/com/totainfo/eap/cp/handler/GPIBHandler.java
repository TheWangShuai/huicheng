package com.totainfo.eap.cp.handler;


import com.totainfo.eap.cp.tcp.server.EchoServerHandler;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelO;
import com.totainfo.eap.cp.util.AsyncUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.UUID;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:4
 */
@Component
public class GPIBHandler {

    private static String address;

    private static EchoServerHandler echoServerHandler;


    public static void changeMode(String modeCmmd) {
        if (modeCmmd.equals("++master")) {
            echoServerHandler.send("GPIB", modeCmmd);
            String format ="++addr 1";
//            String format = "+addr "+address;
            echoServerHandler.send("GPIB", format);
        } else {
            echoServerHandler.send("GPIB", modeCmmd);
        }

    }

    //切换GPIB状态
    public static String changeModeNew(String modeCmmd) {
        String replay = "";
        String evtNo = UUID.randomUUID().toString();
        if (modeCmmd.equals("++master")) {
            replay = echoServerHandler.sendForReply("GPIB", modeCmmd);
            EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
            eapRepCurModelO.setRtnCode("0000000");
            eapRepCurModelO.setRtnMesg("SUCCESS");
            eapRepCurModelO.setState("1");
            ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
            String format ="++addr 1";
            echoServerHandler.send("GPIB", format);
        } else {
            replay = echoServerHandler.sendForReply("GPIB", modeCmmd);
            EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
            eapRepCurModelO.setRtnCode("0000000");
            eapRepCurModelO.setRtnMesg("SUCCESS");
            eapRepCurModelO.setState("0");
            ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
        }
        return replay;
    }

    public static void getDeviceName() {
        EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
        String evtNo = UUID.randomUUID().toString();
        String replay = changeModeNew("++master");
        if (!replay.contains("++master")){
            ClientHandler.sendMessage(evtNo, false, 1, "GPIB切换主机模式失败！" );
            replay = changeModeNew("++device");
            if (!replay.contains("++device")){
                ClientHandler.sendMessage(evtNo, false, 1, "GPIB切换主从模式失败，请按照SOP进行操作！" );
                return;
            }
            eapRepCurModelO.setRtnCode("0000000");
            eapRepCurModelO.setRtnMesg("SUCCESS");
            eapRepCurModelO.setState("0");
            ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
            return;
        }
        eapRepCurModelO.setRtnCode("0000000");
        eapRepCurModelO.setRtnMesg("SUCCESS");
        eapRepCurModelO.setState("1");
        ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
        echoServerHandler.send("GPIB", "sl");
    }

    public static void getParamValue(String paramId) {
        String command = String.format("ur%05d", Integer.parseInt(paramId));
        echoServerHandler.send("GPIB",command);
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

    @Value("${GPIB.address}")
    public void setAddress(String address) {
        GPIBHandler.address = address;
    }
}
