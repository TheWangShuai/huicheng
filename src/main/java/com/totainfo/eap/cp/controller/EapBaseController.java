package com.totainfo.eap.cp.controller;


import com.totainfo.eap.cp.base.service.IEapBaseInterface;
import com.totainfo.eap.cp.base.trx.BaseTrxO;
import com.totainfo.eap.cp.util.GUIDGenerator;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.MatrixAppContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/matrix")
public class EapBaseController {

    @RequestMapping(value = "/sendMsg/{trxId}/{actionFlag}", method = RequestMethod.POST)
    public String sendMessage(@PathVariable String trxId, @PathVariable String actionFlag, @RequestBody String message) {
        long crTime = System.currentTimeMillis();
        String evtNo =  GUIDGenerator.generateInviteCode();
        String returnMsg;
        try{
            IEapBaseInterface baseInterface = (IEapBaseInterface) MatrixAppContext.getBean(trxId);
            returnMsg = baseInterface.subMainProc(evtNo, message);
        }catch (Exception e){
            BaseTrxO baseTrxO = new BaseTrxO();
            baseTrxO.setRtnCode("8999999");
            baseTrxO.setRtnMesg("Not found Service of trxId:[" + trxId + "], please check TrxID");
            returnMsg = JacksonUtils.object2String(baseTrxO);
        }
        returnMsg = "{\"strOutMsg\":" + returnMsg + "}";
        long nxTime = System.currentTimeMillis();
        long dfTime = nxTime - crTime;
        if(dfTime>1000){
            LogUtils.warn("[" + evtNo + "]执行耗时：["+dfTime +"ms]");
        }
        return returnMsg;
    }
}
