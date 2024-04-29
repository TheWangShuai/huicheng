package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.trx.client.EAPReqManualProgram.EAPReqLoadProgramI;
import com.totainfo.eap.cp.trx.client.EAPReqManualProgram.EAPReqLoadProgramO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.*;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author WangShuai
 * @date 2024/4/11
 */
@Service("LTP")
public class EAPReqManualLoadProgram extends EapBaseService<EAPReqLoadProgramI, EAPReqLoadProgramO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private HttpHandler httpHandler;

    @Resource
    private IEqptDao iEqptDao;
    @Override
    public void mainProc(String evtNo, EAPReqLoadProgramI inTrx, EAPReqLoadProgramO outTrx) {

        String actionFlg = inTrx.getActionFlg();
        switch (actionFlg) {
            case "LOADPROGRAM":
                reportManualLoadProgram(evtNo,inTrx, outTrx);
                break;
        }
    }

    private void reportManualLoadProgram(String evtNo, EAPReqLoadProgramI inTrx, EAPReqLoadProgramO outTrx) {

        StateInfo stateInfo = stateDao.getStateInfo();
        String userId = inTrx.getUserId();
        String lotId = inTrx.getLotNo();
        if(StringUtils.isEmpty(lotId)){
            outTrx.setRtnCode(LOT_ID_EMPTY);
            outTrx.setRtnMesg("批次号为空，请重新扫描!");
            return;
        }
        if(StringUtils.isEmpty(userId)){
            outTrx.setRtnCode(USER_ID_EMPTY);
            outTrx.setRtnMesg("操作员ID为空，请输入!");
            return;
        }

        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RTT");
        eapOperationInstructionI.setLotId(lotId);
        eapOperationInstructionI.setUserId(userId);
        eapOperationInstructionI.setIsFirst("1");

        if (Integer.parseInt(stateInfo.getStep()) == 9 && GenergicStatDef.StepStat.COMP.equals(stateInfo.getState())){
            extracted(evtNo, outTrx, lotId, eapOperationInstructionI);
        }else if (Integer.parseInt(stateInfo.getStep()) >= 10 ){
            extracted(evtNo, outTrx, lotId, eapOperationInstructionI);
        }else {
            return;
        }
    }

    private void extracted(String evtNo, EAPReqLoadProgramO outTrx, String lotId, EAPOperationInstructionI eapOperationInstructionI) {
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, testerUrl, eapOperationInstructionI);
        if (StringUtils.isEmpty(returnMesg)) {
            StateSet("10","3", lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
            StateSet("10","3", lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-Tester]: EAP下发手动load程式指令, 程式Loading……");
    }


    public void StateSet(String step, String state, String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }

    public void EapEndCard(String evtNo) {
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
    }

    public void Remove(String evtNo){
        RedisHandler.remove("EQPT:state", "EQPT:%s:LOTINFO".replace("%s", equipmentNo));
        EqptInfo eqptInfo = iEqptDao.getEqpt();
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        RedisHandler.remove("EQPTINFO:EQ:%s:KEY".replace("%s", equipmentNo));
    }

}