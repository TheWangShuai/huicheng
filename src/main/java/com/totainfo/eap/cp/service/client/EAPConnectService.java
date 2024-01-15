package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptStat;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.trx.client.EAPConnect.EAPConnectI;
import com.totainfo.eap.cp.trx.client.EAPConnect.EAPConnectO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:53
 */
@Service("ConnectInfo")
public class EAPConnectService  extends EapBaseService<EAPConnectI, EAPConnectO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IEqptDao eqptDao;



    @Override
    public void mainProc(String evtNo, EAPConnectI inTrx, EAPConnectO outTrx) {
        EqptInfo eqptInfo = eqptDao.getEqpt();
        if(eqptInfo == null){
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(GenericDataDef.equipmentNo);
            eqptInfo.setEqptStat(EqptStat.IDLE);
            eqptInfo.setEqptMode(EqptMode.Online);
            eqptDao.addEqpt(eqptInfo);
        }

        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("ConnectInfo");
        eapSyncEqpInfoI.setTrypeId("I");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo != null){
            eapSyncEqpInfoI.setUserId(lotInfo.getUserId());
            eapSyncEqpInfoI.setLotNo(lotInfo.getLotId());
            eapSyncEqpInfoI.setProberCardId(lotInfo.getProberCard());
        }
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
    }
}
