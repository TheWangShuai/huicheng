package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

/**
 * @author xiaobin.Guo
 * @date 2024年02月26日 16:34
 */
@Component
public class KvmHandler {

    private static HttpHandler httpHandler;

    public static EAPEndCardO eapEndCard(String evtNo){
        EAPEndCardO eapEndCardO;
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        if(StringUtils.isEmpty(returnMesg)){
            eapEndCardO = new EAPEndCardO();
            eapEndCardO.setRtnCode(KVM_TIME_OUT);
            eapEndCardO.setRtnMesg("EAP 发送退Port指令，KVM没有返回");
            return eapEndCardO;
        }
        eapEndCardO = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        return eapEndCardO;
    }

    @Resource
    public void setHttpHandler(HttpHandler httpHandler) {
        KvmHandler.httpHandler = httpHandler;
    }
}
