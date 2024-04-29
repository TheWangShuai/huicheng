package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.trx.client.EAPInitialize.EAPInitializeO;
import com.totainfo.eap.cp.trx.client.EAPInitialize.EAPInitializeI;
import com.totainfo.eap.cp.trx.kvm.EAPChangeControlMode.EAPChangeControlModeO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_RETURN_ERROR;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("Initialize")
public class EAPInitializeService extends EapBaseService<EAPInitializeI, EAPInitializeO> {
    @Resource
    private HttpHandler httpHandler;
    @Override
    public void mainProc(String evtNo, EAPInitializeI inTrx, EAPInitializeO outTrx) {
        EAPInitializeI eapInitializeI = new EAPInitializeI();
        eapInitializeI.setTrxId("EAPACCEPT");
        eapInitializeI.setActionFlg("RST");
        String returnMesage = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapInitializeI);
        if(StringUtils.isEmpty(returnMesage)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP发送指令，尚未check out，关闭实时温度上报和状态变化上报。KVM 没有回复");
            return;
        }
        EAPInitializeO eapInitializeO = JacksonUtils.string2Object(returnMesage, EAPInitializeO.class);
        if(!RETURN_CODE_OK.equals(eapInitializeO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP发送指令，尚未check out，关闭实时温度上报和状态变化上报，KVM 返回失败,原因:[" + eapInitializeO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[Client-EAP-KVM]:下发关闭实时温度采集和状态上报成功");
    }
}
