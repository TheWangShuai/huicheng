package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.GPIBHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportO;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionI;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionIA;
import com.totainfo.eap.cp.trx.kvm.EAPOperationInstruction.EAPOperationInstructionO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 9:56
 */
@Service("deviceNameReport")
public class GPIBDeviceNameReportService extends EapBaseService<GPIBDeviceNameReportI, GPIBDeviceNameReportO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.eap.checkName}")
    private boolean eapCheckName;

    @Override
    public void mainProc(String evtNo, GPIBDeviceNameReportI inTrx, GPIBDeviceNameReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String lotId = lotInfo.getLotId();

        if(eapCheckName){
            String recipeId = lotInfo.getDevice();
            String deviceName = inTrx.getDeviceName();
            if(!recipeId.equals(deviceName)){
                stateset("3", "3", lotId);
                outTrx.setRtnCode(DEVICE_DISMATCH);
                outTrx.setRtnMesg("批次:[" + lotInfo.getLotId() + "]Device校验失败, Device:[" + recipeId + "]，GPIB采集的Device:[" + deviceName + "]，请确认");
                eapEndCard(evtNo);
                remove(evtNo);
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
        }
        //切换被动模式
        GPIBHandler.changeMode("++device");

        stateset("3", "2", lotId);
        EAPOperationInstructionI eapOperationInstructionI = new EAPOperationInstructionI();
        eapOperationInstructionI.setTrxId("EAPACCEPT");
        eapOperationInstructionI.setTrypeId("I");
        eapOperationInstructionI.setActionFlg("RJO");
        eapOperationInstructionI.setOpeType("C");
        List<EAPOperationInstructionIA> lotParamMap1 = lotInfo.getParamList();

        eapOperationInstructionI.setInstructList(lotParamMap1);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapOperationInstructionI);
        stateset("4", "1", lotId);
        if (StringUtils.isEmpty(returnMesg)) {
            stateset("4", "3", lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 没有返回");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        EAPOperationInstructionO eapOperationInstructionO = JacksonUtils.string2Object(returnMesg, EAPOperationInstructionO.class);
        if (!RETURN_CODE_OK.equals(eapOperationInstructionO.getRtnCode())) {
            stateset("4", "3", lotId);
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发代操指令， KVM 返回错误:[" + eapOperationInstructionO.getRtnMesg() + "]");
            eapEndCard(evtNo);
            remove(evtNo);
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP发送代操采集指令成功。");
    }


    public void stateset(String step, String state, String lotno) {
        StateInfo stateInfo1 = new StateInfo();
        stateInfo1.setStep(step);
        stateInfo1.setState(state);
        stateInfo1.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo1);
    }

    public void eapEndCard(String evtNo){
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
    }

    public void remove(String evtNo){
        RedisHandler.remove("EQPT:state", String.format("EQPT:%s:LOTINFO", equipmentNo));
        EqptInfo eqptInfo = eqptDao.getEqpt();
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        RedisHandler.remove(String.format("EQPTINFO:EQ:%s:KEY", equipmentNo));
    }
}
