package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadI;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadO;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInI;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 14:34
 */
@Service("EAPLOTIDREAD")
public class EAPLotIdReadService extends EapBaseService<EAPLotIdReadI, EAPLotIdReadO> {

    @Resource
    private HttpHandler httpHandler;

    @Override
    public void mainProc(String evtNo, EAPLotIdReadI inTrx, EAPLotIdReadO outTrx) {

        String lotId = inTrx.getLotId();
        if(StringUtils.isEmpty(lotId)){
            outTrx.setRtnCode(LOT_ID_EMPTY);
            outTrx.setRtnMesg("批次号为空，请重新扫描!");
            return;
        }
        String proberId = inTrx.getProberId();
        if(StringUtils.isEmpty(proberId)){
            outTrx.setRtnCode(PROBER_ID_EMPTY);
            outTrx.setRtnMesg("探针号为空，请重新扫描!");
            return;
        }
        String userId = inTrx.getUserId();
        if(StringUtils.isEmpty(userId)){
            outTrx.setRtnCode(USER_ID_EMPTY);
            outTrx.setRtnMesg("操作员ID为空，请输入!");
            return;
        }

        EAPReqLotInfoO eapReqLotInfoO = MesHandler.lotInfoReq(lotId, proberId, userId);
        if(!RETURN_CODE_OK.equals(eapReqLotInfoO.getRtnCode())){
            return;
        }

        //todo 发送给前端，LOT 校验成功。

        EAPReqLotInfoOA lotInfo = eapReqLotInfoO.getLotInfo();

        //将信息下发给LVM
        EAPLotInfoWriteInI eapLotInfoWriteInI = new EAPLotInfoWriteInI();
        eapLotInfoWriteInI.setTrxId("EAPLotInfoWriteIn");
        eapLotInfoWriteInI.setActionFlg("RJI");
        eapLotInfoWriteInI.setUserId(userId);
        eapLotInfoWriteInI.setProberCardId(lotInfo.getProberCard());
        eapLotInfoWriteInI.setLoadBoardId(lotInfo.getLoadBoardId());
        eapLotInfoWriteInI.setDeviceId(lotInfo.getDeviceId());
        eapLotInfoWriteInI.setTestProgram(lotInfo.getTestPropram());


        String returnMesg  = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapLotInfoWriteInI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP下发批次信息，KVM没有回复");
            return;
        }


        //todo 发送给前端，LOT信息发送KVM成功

    }
}
