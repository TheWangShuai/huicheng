package com.totainfo.eap.cp.service.kvm;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandO;
import com.totainfo.eap.cp.trx.kvm.KVMSyncConnectionMode.KVMSyncConnectionModeI;
import com.totainfo.eap.cp.trx.kvm.KVMSyncConnectionMode.KVMSyncConnectionModeO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_MESG_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

@Service("KVMSyncConnectionMode")
public class KVMSyncConnectionModeService extends EapBaseService<KVMSyncConnectionModeI, KVMSyncConnectionModeO> {
    @Resource
    private IEqptDao iEqptDao;

    @Resource
    private ILotDao iLotDao;
    @Override
    public void mainProc(String evtNo, KVMSyncConnectionModeI inTrx, KVMSyncConnectionModeO outTrx) {
        EqptInfo eqptInfo = iEqptDao.getEqpt();
        LotInfo curLotInfo = iLotDao.getCurLotInfo();
        if (curLotInfo == null){
            curLotInfo = new LotInfo();
            curLotInfo.setUserId("null");
            curLotInfo.setLotId("null");
            curLotInfo.setProberCard("null");
        }
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setUserId(curLotInfo.getUserId());
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        eapSyncEqpInfoI.setLotNo(curLotInfo.getLotId());
        eapSyncEqpInfoI.setProberCardId(curLotInfo.getProberCard());

        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);

        outTrx.setTrxId("KVMSyncConnectionMode");
        outTrx.setModel(eqptInfo.getEqptMode());
        outTrx.setRtnMesg(RETURN_MESG_OK);
        return;
    }
}
