package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadI;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportIA;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportI;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static com.totainfo.eap.cp.commdef.GenericDataDef.proberUrl;

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
    @Resource
    private IEqptDao iEqptDao;

    private static String proberCardId;
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
        proberCardId = proberId;
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
            EapEndCard(evtNo);
            Remove(evtNo);
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
        lotInfo.setDieCount(eapReqLotInfoOA.getDieCount());
        lotInfo.setParamList(eapReqLotInfoOA.getParamList());
        lotInfo.setTemperatureRange(eapReqLotInfoOA.getTemperatureRange());
        List<EAPReqLotInfoOB> paramList = eapReqLotInfoOA.getParamList();
        String tempValue = null;
        for (EAPReqLotInfoOB eapReqLotInfoOB : paramList){
            if ("Temp".equals(eapReqLotInfoOB.getParamName())){
                tempValue = eapReqLotInfoOB.getParamValue();
            }
        }
        LogUtils.info("mes下发的温度是[{}]",tempValue);
        lotInfo.setTemperature(tempValue);
        lotDao.addLotInfo(lotInfo);

        //上报设备参数信息给ems
        EMSDeviceParameterReportI emsDeviceParameterReportI = new EMSDeviceParameterReportI();
        ArrayList<EMSDeviceParameterReportIA> list = new ArrayList<>();

        EMSDeviceParameterReportIA param1 = new EMSDeviceParameterReportIA();
        param1.setParamCode("1");
        param1.setParamName("温度");
        param1.setParamValue(tempValue);
        list.add(param1);

        EMSDeviceParameterReportIA param2 = new EMSDeviceParameterReportIA();
        param2.setParamCode("2");
        param2.setParamName("温度范围");
        param2.setParamValue(lotInfo.getTemperatureRange());
        list.add(param2);
        emsDeviceParameterReportI.setParamList(list);
        ClientHandler.sendMessage(evtNo,false,2,"[EAP-EMS]:EAP给EMS上报设备参数信息指令成功");
        EmsHandler.emsDeviceParameterReportToEms(evtNo,lotId,emsDeviceParameterReportI);

          //上报信息给RCM
//        EapReportInfoI eapReportInfoI = new EapReportInfoI();
//        eapReportInfoI.setLotId(lotInfo.getLotId());
//        eapReportInfoI.setEquipmentState(GenergicStatDef.EqptStat.RUN);
//        EapReportInfoO eapReportInfoO = RcmHandler.lotInfoReport(evtNo, eapReportInfoI);
//        if(!RETURN_CODE_OK.equals(eapReportInfoO.getRtnCode())){
//            Stateset("2","3",lotId);
//            outTrx.setRtnCode(eapReportInfoO.getRtnCode());
//            outTrx.setRtnMesg("[EAP-RCM]:EAP上报批次信息，RCM返回失败，原因:[" + eapReportInfoO.getRtnMesg() + "]");
//            EapEndCard(evtNo);
//            Remove(evtNo);
//            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
//        }


       //时间校验功能接口
        KVMTimeReportI kvmTimeReportI = new KVMTimeReportI();
        kvmTimeReportI.setTrxId("EAPACCEPT");
        kvmTimeReportI.setActionFlg("TIME");
        kvmTimeReportI.setEqpId(equipmentNo);
        String returnMsg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmTimeReportI);
        if(StringUtils.isEmpty(returnMsg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报指令，KVM没有回复");
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        KVMTimeReportO kvmTimeReportO = JacksonUtils.string2Object(returnMsg, KVMTimeReportO.class);
        if(!RETURN_CODE_OK.equals(kvmTimeReportO.getRtnCode())){
            outTrx.setRtnCode(kvmTimeReportO.getRtnCode());
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报信息，KVM返回失败，原因:[" + kvmTimeReportO.getRtnMesg() + "]");
            Remove(evtNo);
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo,false,2,"EAP开始进行时间校验");

    }
    public void Stateset(String step, String state,String lotno) {
        StateInfo stateInfo = new StateInfo();
        stateInfo.setStep(step);
        stateInfo.setState(state);
        stateInfo.setLotNo(lotno);
        stateDao.addStateInfo(stateInfo);
    }
    public void EapEndCard(String evtNo){
        EAPEndCardI eapEndCardI = new EAPEndCardI();
        eapEndCardI.setTrxId("EAPACCEPT");
        eapEndCardI.setActionFlg("RTL");
        String returnMesg = httpHandler.postHttpForEqpt(evtNo, proberUrl, eapEndCardI);
        EAPEndCardO eapEndCardO1 = JacksonUtils.string2Object(returnMesg, EAPEndCardO.class);
        EAPEndCardO eapEndCardO = new EAPEndCardO();
        eapEndCardO.setRtnMesg(eapEndCardO1.getRtnMesg());
    }
    public void Remove(String evtNo){
        RedisHandler.remove("EQPT:state", "EQPT:%s:LOTINFO".replace("%s", equipmentNo));
        EqptInfo eqptInfo = iEqptDao.getEqpt();
        EAPSyncEqpInfoI eapSyncEqpInfoI = new EAPSyncEqpInfoI();
        eapSyncEqpInfoI.setTrxId("RtnConnectInfo");
        eapSyncEqpInfoI.setActionFlg("RLC");
        eapSyncEqpInfoI.setState(eqptInfo.getEqptStat());
        eapSyncEqpInfoI.setModel(eqptInfo.getEqptMode());
        ClientHandler.sendEqpInfo(evtNo, eapSyncEqpInfoI);
        RedisHandler.remove("EQPTINFO:EQ:%s:KEY".replace("%s", equipmentNo));
    }

    public static String getProberCardId() {
        return proberCardId;
    }
}
