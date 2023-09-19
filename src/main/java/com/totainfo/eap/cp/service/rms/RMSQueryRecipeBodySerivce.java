package com.totainfo.eap.cp.service.rms;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.trx.kvm.EAPDeviceParamCollection.EAPDeviceParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPDeviceParamCollection.EAPDeviceParamCollectionO;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody.RmsQueryRecipeBodyI;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody.RmsQueryRecipeBodyO;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.EQPT_MODE_DISMATCH;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_RETURN_ERROR;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 12:20
 */
@Service("rmsQueryRecipeBody")
public class RMSQueryRecipeBodySerivce  extends EapBaseService<RmsQueryRecipeBodyI, RmsQueryRecipeBodyO> {

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.timeout}")
    private long timeOut;

    @Override
    public void mainProc(String evtNo, RmsQueryRecipeBodyI inTrx, RmsQueryRecipeBodyO outTrx) {
        EqptInfo eqptInfo = eqptDao.getEqpt();
        if(!EqptMode.Online.equals(eqptInfo.getEqptMode())){
            outTrx.setRtnCode(EQPT_MODE_DISMATCH);
            outTrx.setRtnMesg("设备当前是Offline 模式，请确认");
            return;
        }

        String recipeId = inTrx.getRecipeId();
        EAPDeviceParamCollectionI eapDeviceParamCollectionI = new EAPDeviceParamCollectionI();
        eapDeviceParamCollectionI.setTrxId("EAPACCEPT");
        eapDeviceParamCollectionI.setTrypeId("I");
        eapDeviceParamCollectionI.setActionFlg("RWPEE");
        eapDeviceParamCollectionI.setDeviceName(recipeId);
        eapDeviceParamCollectionI.setRequestKey(evtNo);
        AsyncUtils.setRequest(evtNo, timeOut);
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapDeviceParamCollectionI);
        if(StringUtils.isEmpty(returnMesg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 下发Devce Name:["+recipeId+"]参数采集， KVM 没有返回");
            return;
        }
        EAPDeviceParamCollectionO eapDeviceParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPDeviceParamCollectionO.class);
        if(!RETURN_CODE_OK.equals(eapDeviceParamCollectionO.getRtnCode())){
            outTrx.setRtnCode(KVM_RETURN_ERROR);
            outTrx.setRtnMesg("EAP 下发Devce Name:["+recipeId+"]参数采集， KVM 返回错误:[" + eapDeviceParamCollectionO.getRtnMesg() + "]");
            return;
        }
        String recipeBody = AsyncUtils.getResponse(evtNo, timeOut);
        outTrx.setRecipeBody(recipeBody);
    }
}
