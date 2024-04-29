package com.totainfo.eap.cp.service.rms;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
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
            outTrx.setRtnMesg("[RMS-EAP]:设备当前是Offline 模式，请确认");
            return;
        }

        String recipeId = inTrx.getRecipeId();
//        String toolType = inTrx.getToolType();
        String toolType = "AP3000";
        RmsQueryRecipeBodyI rmsQueryRecipeBodyI = new RmsQueryRecipeBodyI();
        rmsQueryRecipeBodyI.setRecipeId(recipeId);
        rmsQueryRecipeBodyI.setToolType("AP3000");
        ClientHandler.sendMessage(evtNo,false,2,"EAP向Clinet端发送采集recipebody成功");
        String recipeBody = httpHandler.getbodyHttpForClient(evtNo, GenericDataDef.recipeBodyUrl, recipeId, toolType,rmsQueryRecipeBodyI);
        if(StringUtils.isEmpty(recipeBody)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-Client]:EAP 下发Recipe :["+recipeId+"]参数采集， Client 没有返回");
            return;
        }
        outTrx.setRecipeId(recipeId);
        outTrx.setToolId(inTrx.getToolId());
        outTrx.setRecipeBody(recipeBody);

//        EAPDeviceParamCollectionI eapDeviceParamCollectionI = new EAPDeviceParamCollectionI();
//        eapDeviceParamCollectionI.setTrxId("client");
//        eapDeviceParamCollectionI.setTrypeId("I");
//        eapDeviceParamCollectionI.setActionFlg("RWPEE");
//        eapDeviceParamCollectionI.setDeviceName(recipeId);
//        eapDeviceParamCollectionI.setRequestKey(evtNo);
//        AsyncUtils.setRequest(evtNo, timeOut);
//        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapDeviceParamCollectionI);
//        if(StringUtils.isEmpty(returnMesg)){
//            outTrx.setRtnCode(KVM_TIME_OUT);
//            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发Devce Name:["+recipeId+"]参数采集， KVM 没有返回");
//            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
//            return;
//        }
//        EAPDeviceParamCollectionO eapDeviceParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPDeviceParamCollectionO.class);
//        if(!RETURN_CODE_OK.equals(eapDeviceParamCollectionO.getRtnCode())){
//            outTrx.setRtnCode(KVM_RETURN_ERROR);
//            outTrx.setRtnMesg("[EAP-KVM]:EAP 下发Devce Name:["+recipeId+"]参数采集， KVM 返回错误:[" + eapDeviceParamCollectionO.getRtnMesg() + "]");
//            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
//            return;
//        }
//        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:EAP 发送Device param采集指令成功。");
//        String recipeBody = AsyncUtils.getResponse(evtNo, timeOut);
//        outTrx.setRecipeBody(recipeBody);
    }
}
