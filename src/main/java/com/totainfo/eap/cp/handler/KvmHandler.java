package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.KVMReqHalt.KVMReqHaltI;
import com.totainfo.eap.cp.trx.kvm.KVMReqHalt.KVMReqHaltO;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyI;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyO;
import com.totainfo.eap.cp.trx.rcm.EapReportMESDataInfoI;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenericDataDef.*;

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

    public static CleanFuncKeyO cleanFuncKey(String evtNo, String cleanFlg){
        CleanFuncKeyI cleanFuncKeyI = new CleanFuncKeyI();
        CleanFuncKeyO cleanFuncKeyO = new CleanFuncKeyO();
        cleanFuncKeyI.setTrxId("EAPACCEPT");
        cleanFuncKeyI.setActionFlg("RTC");
        cleanFuncKeyI.setClearFlg(cleanFlg);
        cleanFuncKeyI.setEquipmentNo(equipmentNo);
        String returnMsg = httpHandler.postHttpForEqpt(evtNo, testerUrl, cleanFuncKeyI);

        if (StringUtils.isEmpty(returnMsg)) {
            cleanFuncKeyO.setRtnCode(KVM_TIME_OUT);
            cleanFuncKeyO.setRtnMesg("EAP发送FuncKey清除， KVM没有返回！");
            ClientHandler.sendMessage(evtNo, false, 1, cleanFuncKeyO.getRtnMesg());
        }else {

            cleanFuncKeyO = JacksonUtils.string2Object(returnMsg, CleanFuncKeyO.class);
        }

        return cleanFuncKeyO;
    }

    // 给tester机台下发mesData数据
    public static void reportMESData(String evtNo, String mesData){
        EapReportMESDataInfoI eapReportMESDataInfoI = new EapReportMESDataInfoI();
        eapReportMESDataInfoI.setTrxId("EAPACCEPT");
        eapReportMESDataInfoI.setActionFlg("MESDATA");
        eapReportMESDataInfoI.setMesData(mesData);
        httpHandler.postHttpForEqpt(evtNo, testerUrl, eapReportMESDataInfoI);
    }

    // 给KVM下发Stop指令
    public static KVMReqHaltO haltStop(String evtNo){
        KVMReqHaltI kvmReqHaltI = new KVMReqHaltI();
        KVMReqHaltO kvmReqHaltO;
        kvmReqHaltI.setTrxId("EAPACCEPT");
        kvmReqHaltI.setActionFlg("STOP");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmReqHaltI);
        if(StringUtils.isEmpty(returnMesg)){
            kvmReqHaltO = new KVMReqHaltO();
            kvmReqHaltO.setRtnCode(KVM_TIME_OUT);
            kvmReqHaltO.setRtnMesg("EAP发送停机指令，KVM没有返回");
            return kvmReqHaltO;
        }
        kvmReqHaltO = JacksonUtils.string2Object(returnMesg, KVMReqHaltO.class);
        return kvmReqHaltO;
    }

    @Resource
    public void setHttpHandler(HttpHandler httpHandler) {
        KvmHandler.httpHandler = httpHandler;
    }
}
