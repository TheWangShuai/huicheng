package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.service.kvm.KVMOperateEndService;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlI;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlO;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndI;
import com.totainfo.eap.cp.trx.kvm.KVMOperateend.KVMOperateEndO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInI;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.testerUrl;


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
    private HttpHandler httpHandler;

    @Override
    public void mainProc(String evtNo, EAPEqpControlI inTrx, EAPEqpControlO outTrx) {
        String userId = inTrx.getUserId();
        String model = inTrx.getModel();
        boolean isCheckIn = inTrx.getIsCheckIn();

        if (isCheckIn) {
            LogUtils.info("开始check in");
            LotInfo lotInfo = lotDao.getCurLotInfo();
            if (lotInfo == null) {
                outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
                outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
                ClientHandler.sendMessage(evtNo, true, 1, outTrx.getRtnMesg());
                return;
            }
            String lotId = lotInfo.getLotId();
            Stateset("6","1",lotId);
            EAPReqCheckInI eapReqCheckInI = new EAPReqCheckInI();
            eapReqCheckInI.setTrxId("checkIn");
            eapReqCheckInI.setEvtUsr(userId);
            eapReqCheckInI.setLotNo(lotInfo.getLotId());
            eapReqCheckInI.setEquipmentNo(GenericDataDef.equipmentNo);
            eapReqCheckInI.setProbeCard(lotInfo.getProberCard().split("-")[0]);

            eapReqCheckInI.setTemperature(KVMOperateEndService.getStr());
            eapReqCheckInI.setTestProgram(lotInfo.getTestProgram());
            eapReqCheckInI.setDevice(KVMOperateEndService.getDn());

            EAPReqCheckInO eapReqCheckInO = MesHandler.checkInReq(evtNo, lotInfo.getLotId(), userId, eapReqCheckInI);
            if (!RETURN_CODE_OK.equals(eapReqCheckInO.getRtnCode())) {
                Stateset("6","3",lotId);
                outTrx.setRtnCode(eapReqCheckInO.getRtnCode());
                outTrx.setRtnMesg(eapReqCheckInO.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] Check In 成功。");
            Stateset("6","2",lotId);
            EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
            eapOperationInstructionI.setTrxId("EAPACCEPT");
            eapOperationInstructionI.setTrypeId("I");
            eapOperationInstructionI.setActionFlg("RTT");
            eapOperationInstructionI.setLotId(lotInfo.getLotId());
            eapOperationInstructionI.setUserId("HF0731");

            String returnMesg = httpHandler.postHttpForEqpt(evtNo, testerUrl, eapOperationInstructionI);
            Stateset("7","1",lotId);
            if (StringUtils.isEmpty(returnMesg)) {
                Stateset("7","3",lotId);
                outTrx.setRtnCode(KVM_TIME_OUT);
                outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 没有返回");
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
            if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
                Stateset("7","3",lotId);
                outTrx.setRtnCode(KVM_RETURN_ERROR);
                outTrx.setRtnMesg("EAP 下发测试程序清除， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
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
}
