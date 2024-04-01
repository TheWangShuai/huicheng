package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.GPIBHandler;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelI;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelO;
import org.springframework.stereotype.Service;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
@Service("GPIBSLAVEMODE")
public class EAPReqChangeGPIBModel  extends EapBaseService<EAPChangeGPIBModelI, EAPChangeGPIBModelO> {


    @Override
    public void mainProc(String evtNo, EAPChangeGPIBModelI inTrx, EAPChangeGPIBModelO outTrx) {

        String reply = GPIBHandler.changeModeNew( "++device");
        if (reply.contains("++device")){
            outTrx.setRtnCode("0000000");
            outTrx.setRtnCode("SUCCESS");
            ClientHandler.changeGPIBMode(evtNo,outTrx);
        }
        ClientHandler.sendMessage(evtNo,false,1,"EAP下发切换从机模式成功！");
    }
}