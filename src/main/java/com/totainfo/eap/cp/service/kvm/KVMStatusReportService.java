package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.impl.EqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.kvm.KVMStatusReport.KVMStatusReportI;
import com.totainfo.eap.cp.trx.kvm.KVMStatusReport.KVMStatusReportO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 13:55
 */
@Service("KVMEquipmentState")
public class KVMStatusReportService extends EapBaseService<KVMStatusReportI, KVMStatusReportO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IEqptDao eqptDao;


    @Override
    public void mainProc(String evtNo, KVMStatusReportI inTrx, KVMStatusReportO outTrx) {


        String eqptStat = inTrx.getState();
        EqptInfo eqptInfo = eqptDao.getEqptWithLock();
        if(eqptInfo == null){
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(GenericDataDef.equipmentNo);
            eqptInfo.setEqptMode(EqptMode.Offline);
        }

        //状态没变不做处理
        if(eqptStat.equals(eqptInfo.getEqptStat())){
            return;
        }
        eqptInfo.setEqptStat(eqptStat);
        eqptDao.addEqpt(eqptInfo);


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
        MesHandler.eqptStatReport(evtNo, eqptStat, "",lotInfo.getUserId());
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
    }
}
