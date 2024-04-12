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
@Service("ChangeGPIBState")
public class EAPReqChangeGPIBModel  extends EapBaseService<EAPChangeGPIBModelI, EAPChangeGPIBModelO> {


    @Override
    public void mainProc(String evtNo, EAPChangeGPIBModelI inTrx, EAPChangeGPIBModelO outTrx) {

        String actionFlg = inTrx.getActionFlg();
        switch (actionFlg) {
            case "GPIBSLAVEMODEL":
                clientReportChangeGPIBState(evtNo, outTrx);
                break;
        }
    }

    private void clientReportChangeGPIBState(String evtNo,EAPChangeGPIBModelO outTrx) {
        GPIBHandler.changeModeNew("++device");
        ClientHandler.sendMessage(evtNo, false, 2, "EAP下发切换从机模式成功！");
    }
}