package com.totainfo.eap.cp.service.rms;

import com.rabbitmq.tools.json.JSONUtil;
import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionI;
import com.totainfo.eap.cp.trx.kvm.EAPSingleParamCollection.EAPSingleParamCollectionO;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody.RmsQueryRecipeBodyI;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeIdList.RmsQueryRecipeIdListI;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeIdList.RmsQueryRecipeIdListO;
import com.totainfo.eap.cp.trx.rms.RmsQueryRecipeIdList.RmsQueryRecipeIdListOA;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.EQPT_MODE_DISMATCH;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_RETURN_ERROR;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

/**
 * @author xiaobin.Guo
 * @date 2023年09月19日 11:56
 */
@Service("rmsQueryRecipeIdList")
public class RMSQueryRecipeIdListService extends EapBaseService<RmsQueryRecipeIdListI, RmsQueryRecipeIdListO> {

    @Resource
    private IEqptDao eqptDao;

    @Resource
    private HttpHandler httpHandler;

    @Value("${spring.rabbitmq.timeout}")
    private long timeOut;

    @Override
    public void mainProc(String evtNo, RmsQueryRecipeIdListI inTrx, RmsQueryRecipeIdListO outTrx) {
        EqptInfo eqptInfo = eqptDao.getEqpt();
        if (!EqptMode.Online.equals(eqptInfo.getEqptMode())) {
            outTrx.setRtnCode(EQPT_MODE_DISMATCH);
            outTrx.setRtnMesg("[EAP-RMS]:设备当前是Offline 模式，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String toolType = "AP3000";
        RmsQueryRecipeIdListI rmsQueryRecipeIdListI = new RmsQueryRecipeIdListI();
        rmsQueryRecipeIdListI.setToolType(toolType);
        ClientHandler.sendMessage(evtNo,false,2,"EAP向Clinet端发送采集recipeList成功");
        String recipeIdList = httpHandler.getlistHttpForClient(evtNo, GenericDataDef.recipeBodyUrl, toolType,rmsQueryRecipeIdListI);
        LogUtils.info("收到的消息是[{}]", recipeIdList);
        RmsQueryRecipeIdListI rmsQueryRecipeIdListI1 = JacksonUtils.string2Object(recipeIdList, RmsQueryRecipeIdListI.class);
        LogUtils.info("转成的对象是[{}]",rmsQueryRecipeIdListI1);
        String data = rmsQueryRecipeIdListI1.getData();
        List<String> list = JacksonUtils.string2Object(data, List.class);
//        List<String> dataList = Arrays.asList(data.split(","));
        LogUtils.info("data[{}]",data);
        if(recipeIdList ==null){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-Client]:EAP 下发RecipeList采集， Client 没有返回");
            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
            return;
        }
        RmsQueryRecipeIdListOA rmsQueryRecipeIdListOA;
        List<RmsQueryRecipeIdListOA> rmsQueryRecipeIdListOAS = new ArrayList<>(list.size());
        for(String recipeId: list){
            rmsQueryRecipeIdListOA = new RmsQueryRecipeIdListOA();
            rmsQueryRecipeIdListOA.setRecipeId(recipeId);
            rmsQueryRecipeIdListOAS.add(rmsQueryRecipeIdListOA);
        }
        LogUtils.info("返回的list[{}]",rmsQueryRecipeIdListOAS);
        outTrx.setBisRecipeVOList(rmsQueryRecipeIdListOAS);



//        EAPSingleParamCollectionI eapSingleParamCollectionI = new EAPSingleParamCollectionI();
//        eapSingleParamCollectionI.setTrxId("EAPACCEPT");
//        eapSingleParamCollectionI.setActionFlg("RWPE");
//        eapSingleParamCollectionI.setRequestKey(evtNo);
//        eapSingleParamCollectionI.setParameterName("");
//
//
//        AsyncUtils.setRequest(evtNo, timeOut);
//        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapSingleParamCollectionI);
//        if(StringUtils.isEmpty(returnMesg)){
//            outTrx.setRtnCode(KVM_TIME_OUT);
//            outTrx.setRtnMesg("[EAP-KVM]:EAP 发送采集Device Name， KVM 没有返回");
//            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
//            return;
//        }
//        EAPSingleParamCollectionO eapSingleParamCollectionO = JacksonUtils.string2Object(returnMesg, EAPSingleParamCollectionO.class);
//        if(!RETURN_CODE_OK.equals(eapSingleParamCollectionO.getRtnCode())){
//            outTrx.setRtnCode(KVM_RETURN_ERROR);
//            outTrx.setRtnMesg("[EAP-KVM]:EAP 发送采集Device Name， KVM 返回错误:[" + eapSingleParamCollectionO.getRtnMesg() + "]");
//            ClientHandler.sendMessage(evtNo,false,2,outTrx.getRtnMesg());
//            return;
//        }
//        List<String> recipeIdList = AsyncUtils.getResponse(evtNo, timeOut);
//        if(recipeIdList == null){
//            recipeIdList = new ArrayList<>(0);
//        }
//
//
//        RmsQueryRecipeIdListOA rmsQueryRecipeIdListOA;
//        List<RmsQueryRecipeIdListOA> rmsQueryRecipeIdListOAS = new ArrayList<>();
//        rmsQueryRecipeIdListOA = new RmsQueryRecipeIdListOA();
//        rmsQueryRecipeIdListOA.setRecipeId("8163002A-2AU6A12");
//        rmsQueryRecipeIdListOAS.add(rmsQueryRecipeIdListOA);
//
//
//        outTrx.setBisRecipeVOList(rmsQueryRecipeIdListOAS);
    }
}
