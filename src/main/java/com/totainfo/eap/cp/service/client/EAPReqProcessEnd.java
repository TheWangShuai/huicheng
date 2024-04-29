package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.trx.client.EAPReqProcess.EAPReqProcessI;
import com.totainfo.eap.cp.trx.client.EAPReqProcess.EAPReqProcessO;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WangShuai
 * @date 2024/4/13
 */
@Service("MSJ")
public class EAPReqProcessEnd extends EapBaseService<EAPReqProcessI, EAPReqProcessO> {

    @Resource
    private LotDao lotDao;
    @Resource
    private StateDao stateDao;
    @Override
    public void mainProc(String evtNo, EAPReqProcessI inTrx, EAPReqProcessO outTrx) {

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