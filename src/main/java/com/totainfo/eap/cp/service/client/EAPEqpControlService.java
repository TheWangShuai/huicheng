package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.service.kvm.KVMOperateEndService;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlI;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.*;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;


/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:34
 */
@Service("StartOrStop")
public class EAPEqpControlService extends EapBaseService<EAPEqpControlI, EAPEqpControlO> {
    @Resource
    private ILotDao lotDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private ClientHandler clientHandler;

    @Resource
    private HttpHandler httpHandler;
    @Resource
    private IEqptDao iEqptDao;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Override
    public void mainProc(String evtNo, EAPEqpControlI inTrx, EAPEqpControlO outTrx) {
        String userId = inTrx.getUserId();
        String model = inTrx.getModel();
        boolean isCheckIn = inTrx.getIsCheckIn();
        //第九步开始CheckIn开始
        clientHandler.setFlowStep(GenergicStatDef.StepName.NIGHT, GenergicStatDef.StepStat.INPROCESS);
        if (isCheckIn) {
            LogUtils.info("开始check in");
            LotInfo lotInfo = lotDao.getCurLotInfo();
            if (lotInfo == null) {
                outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
                outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
                EapEndCard(evtNo);
                ClientHandler.sendMessage(evtNo, true, 1, outTrx.getRtnMesg());
                return;
            }
            String lotId = lotInfo.getLotId();
            EAPReqCheckInI eapReqCheckInI = new EAPReqCheckInI();
            eapReqCheckInI.setTrxId("checkIn");
            eapReqCheckInI.setEvtUsr(userId);
            eapReqCheckInI.setLotNo(lotInfo.getLotId());
            eapReqCheckInI.setEquipmentNo(GenericDataDef.equipmentNo);
            eapReqCheckInI.setProbeCard(lotInfo.getProberCard().split("-")[0]);
            eapReqCheckInI.setTemperature(lotInfo.getTemperature());
            eapReqCheckInI.setTestProgram(lotInfo.getTestProgram());
            eapReqCheckInI.setDevice(lotInfo.getDevice());
            LogUtils.info("[{}]",eapReqCheckInI);
            EAPReqCheckInO eapReqCheckInO = MesHandler.checkInReq(evtNo, lotInfo.getLotId(), userId, eapReqCheckInI);
            if (!RETURN_CODE_OK.equals(eapReqCheckInO.getRtnCode())) {
                Stateset("9","3",lotId);
                outTrx.setRtnCode(eapReqCheckInO.getRtnCode());
                outTrx.setRtnMesg(eapReqCheckInO.getRtnMesg());
                EapEndCard(evtNo);
                Remove(evtNo);
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] Check In 成功。");
            EmsHandler.reportRunWorkInfo(evtNo,"CheckIn成功",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
            //第九步开始CheckIn结束
            clientHandler.setFlowStep(GenergicStatDef.StepName.NIGHT, GenergicStatDef.StepStat.COMP);

            //EAP向KVM下发load程式指令
            EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
            eapOperationInstructionI.setTrxId("EAPACCEPT");
            eapOperationInstructionI.setTrypeId("I");
            eapOperationInstructionI.setActionFlg("RTT");
            eapOperationInstructionI.setLotId(lotInfo.getLotId());
            eapOperationInstructionI.setUserId(userId);
            eapOperationInstructionI.setIsFirst("0");

            String returnMesg = httpHandler.postHttpForEqpt(evtNo, testerUrl, eapOperationInstructionI);
            if (StringUtils.isEmpty(returnMesg)) {
                Stateset("10","3",lotId);
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 没有返回");
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
            if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
                Stateset("10","3",lotId);
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
//                EapEndCard(evtNo);
//                Remove(evtNo);
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "EAP成功下发load程式指令");
        } else {
            EAPControlCommandI eapControlCommandI = new EAPControlCommandI();
            eapControlCommandI.setTrxId("EAPACCEPT");
            eapControlCommandI.setActionFlg("RJPI");
            eapControlCommandI.setUserId(userId);
            eapControlCommandI.setModel(model);

            String returnMsg = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapControlCommandI);
            if (StringUtils.isEmpty(returnMsg)) {
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("[EAP-KVM]:EAP 发送设备启停指令，KVM 没有回复");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }
            EAPControlCommandO eapControlCommandO = JacksonUtils.string2Object(returnMsg, EAPControlCommandO.class);
            if (!RETURN_CODE_OK.equals(eapControlCommandO.getRtnCode())) {
                outTrx.setRtnCode(eapControlCommandO.getRtnCode());
                outTrx.setRtnMesg("[EAP-KVM]:EAP 发送设备启停指令，KVM 返回失败，原因:[" + eapControlCommandO.getRtnMesg() + "]");
                ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
                return;
            }

            String statDes = "0".equals(model) ? "启动" : "停止";
            ClientHandler.sendMessage(evtNo, false, 2, "EAP 发送设备启停指令成功，当前状态:[" + statDes + "]");
        }
    }
    public void Stateset(String step, String state,String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }
    public void EapEndCard(String evtNo){
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
