package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.impl.EqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.trx.client.EAPContrlModeChange.EAPContrlModeChangeI;
import com.totainfo.eap.cp.trx.client.EAPContrlModeChange.EAPControlModeChangeO;
import com.totainfo.eap.cp.trx.kvm.EAPChangeControlMode.EAPChangeControlModeI;
import com.totainfo.eap.cp.trx.kvm.EAPChangeControlMode.EAPChangeControlModeO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_RETURN_ERROR;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 9:50
 */
@Service("ControlModeChange")
public class EAPControlModeChangeService extends EapBaseService<EAPContrlModeChangeI, EAPControlModeChangeO> {

    @Resource
    private EqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Override
    public void mainProc(String evtNo, EAPContrlModeChangeI inTrx, EAPControlModeChangeO outTrx) {
        String userId = inTrx.getUserId();
        String model = inTrx.getModel();
        EqptInfo eqptInfo = eqptDao.getEqptWithLock();
        if(eqptInfo == null){
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(GenericDataDef.equipmentNo);
        }
        eqptInfo.setEqptMode(model);
        eqptDao.addEqpt(eqptInfo);

        EAPChangeControlModeI eapChangeControlModeI = new EAPChangeControlModeI();
        eapChangeControlModeI.setTrxId("EAPACCEPT");
        eapChangeControlModeI.setActionFlg("RLC");
        eapChangeControlModeI.setUserId(userId);
        eapChangeControlModeI.setModel(model);

        String returnMesg = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.proberUrl, eapChangeControlModeI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP切换模式，KVM 没有回复");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        EAPChangeControlModeO eapChangeControlModeO = JacksonUtils.string2Object(returnMesg, EAPChangeControlModeO.class);
        if(!RETURN_CODE_OK.equals(eapChangeControlModeO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("[EAP-KVM]:EAP切换模式，KVM 返回失败,原因:[" + eapChangeControlModeO.getRtnMesg() + "]");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }

        String modeDesc = "0".equals(model)? "offline":"online";
        ClientHandler.sendMessage(evtNo, false, 2, "[Client-EAP-KVM]:模式切换成功，当前模式:[" + modeDesc + "]");

    }
}
