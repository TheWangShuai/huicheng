package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.GPIBHandler;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelI;
import com.totainfo.eap.cp.trx.client.EAPChangeGPIBModel.EAPChangeGPIBModelO;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;import java.lang.ref.PhantomReference;

/**
 * @author WangShuai
 * @date 2024/3/30
 */
@Service("ChangeGPIBState")
public class EAPReqChangeGPIBModel  extends EapBaseService<EAPChangeGPIBModelI, EAPChangeGPIBModelO> {

    @Resource
    private LotDao lotDao;

    @Resource
    private StateDao stateDao;

    @Override
    public void mainProc(String evtNo, EAPChangeGPIBModelI inTrx, EAPChangeGPIBModelO outTrx) {

        String actionFlg = inTrx.getActionFlg();
        switch (actionFlg) {
            case "GPIBSLAVEMODEL":
                clientReportChangeGPIBState(evtNo,inTrx, outTrx);
                break;
        }
    }

    private void clientReportChangeGPIBState(String evtNo, EAPChangeGPIBModelI inTrx, EAPChangeGPIBModelO outTrx) {
        GPIBHandler.changeModeNew("++device");
        ClientHandler.sendMessage(evtNo, false, 2, "EAP下发切换从机模式成功！");
        String actionFlg = inTrx.getActionFlg();
        LotInfo curLotInfo = lotDao.getCurLotInfo();
        if ("ManualStopJob".equals(actionFlg)){
            EmsHandler.waferInfotoEms(evtNo,curLotInfo.getLotId(),curLotInfo.getWaferLot(),"End");
            ClientHandler.sendMessage(evtNo,false,2,"批次: [" + curLotInfo.getLotId() + "], 用户[" + curLotInfo.getUserId() + "]手动制程结束" );
            removeCache();
        }
    }

    public void removeCache() {
        lotDao.removeLotInfo();
        stateDao.removeState();
    }
}