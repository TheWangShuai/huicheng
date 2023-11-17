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
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadI;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadO;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInI;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 14:34
 */
@Service("LotIdRead")
public class EAPLotIdReadService extends EapBaseService<EAPLotIdReadI, EAPLotIdReadO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IStateDao stateDao;
    @Resource
    private HttpHandler httpHandler;

    @Override
    public void mainProc(String evtNo, EAPLotIdReadI inTrx, EAPLotIdReadO outTrx) {
        String lotId = inTrx.getLotNo();
        if(StringUtils.isEmpty(lotId)){
            Stateset("1","3",lotId);
            outTrx.setRtnCode(LOT_ID_EMPTY);
            outTrx.setRtnMesg("批次号为空，请重新扫描!");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }

        Stateset("1","1",lotId);

        String proberId = inTrx.getProberCardId();
        if(StringUtils.isEmpty(proberId)){
            Stateset("1","3",lotId);
            outTrx.setRtnCode(PROBER_ID_EMPTY);
            outTrx.setRtnMesg("探针号为空，请重新扫描!");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        String userId = inTrx.getUserId();
        if(StringUtils.isEmpty(userId)){
            Stateset("1","3",lotId);
            outTrx.setRtnCode(USER_ID_EMPTY);
            outTrx.setRtnMesg("操作员ID为空，请输入!");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo != null){
            Stateset("1","3",lotId);
            outTrx.setRtnCode(LOT_INFO_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:批次:[" + lotInfo.getLotId() + "]制程未结束，请等待");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }

        EAPReqLotInfoO eapReqLotInfoO = MesHandler.lotInfoReq(evtNo, lotId, proberId, userId);
        if(!RETURN_CODE_OK.equals(eapReqLotInfoO.getRtnCode())){
            Stateset("1","3",lotId);
            return;
        }
        //发送给前端，LOT 校验成功。
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]:批次号:[" + lotId + "]探针:["+ proberId +"] MES 验证成功。");
        Stateset("1","2",lotId);
        EAPReqLotInfoOA eapReqLotInfoOA = eapReqLotInfoO.getLotInfo();
        lotInfo = new LotInfo();
        lotInfo.setLotId(lotId);
        lotInfo.setWaferLot(eapReqLotInfoOA.getWaferLot());
        lotInfo.setDevice(eapReqLotInfoOA.getDevice());
        lotInfo.setLoadBoardId(eapReqLotInfoOA.getLoadBoardId());
        lotInfo.setProberCard(eapReqLotInfoOA.getProbeCard());
        lotInfo.setTestProgram(eapReqLotInfoOA.getTestProgram());
        lotInfo.setDeviceId(eapReqLotInfoOA.getDeviceId());
        lotInfo.setUserId(userId);
        lotInfo.setParamList(eapReqLotInfoOA.getParamList());
        lotInfo.setTemperatureRange(eapReqLotInfoOA.getTemperatureRange());
        lotInfo.setTemperature(eapReqLotInfoOA.getParamList().get(2).getParamValue());
        lotDao.addLotInfo(lotInfo);

        //将信息下发给KVM
        EAPLotInfoWriteInI eapLotInfoWriteInI = new EAPLotInfoWriteInI();
        eapLotInfoWriteInI.setTrxId("EAPACCEPT");
        eapLotInfoWriteInI.setActionFlg("RJI");
        eapLotInfoWriteInI.setUserId(userId);
        eapLotInfoWriteInI.setProberCardId(inTrx.getProberCardId());
        eapLotInfoWriteInI.setLoadBoardId(lotInfo.getLoadBoardId());
        eapLotInfoWriteInI.setWaferLot(lotInfo.getWaferLot());
        eapLotInfoWriteInI.setDeviceId(lotInfo.getDeviceId());
        eapLotInfoWriteInI.setTestProgram(lotInfo.getTestProgram());
        String returnMesg  = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapLotInfoWriteInI);
        Stateset("2","1",lotId);
        if(StringUtils.isEmpty(returnMesg)){
            Stateset("2","3",lotId);
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM没有回复");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        EAPLotInfoWriteInO eapLotInfoWriteInO = JacksonUtils.string2Object(returnMesg, EAPLotInfoWriteInO.class);
        if(!RETURN_CODE_OK.equals(eapLotInfoWriteInO.getRtnCode())){
            Stateset("2","3",lotId);
            outTrx.setRtnCode(eapLotInfoWriteInO.getRtnCode());
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发批次信息，KVM返回失败，原因:[" + eapLotInfoWriteInO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
        }
        //发送给前端，LOT信息发送KVM成功
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:批次:[" + lotId + "]信息下发KVM成功。");
    }
    public void Stateset(String step, String state,String lotno) {
        StateInfo stateInfo = new StateInfo();
        stateInfo.setStep(step);
        stateInfo.setState(state);
        stateInfo.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo);
    }
}
