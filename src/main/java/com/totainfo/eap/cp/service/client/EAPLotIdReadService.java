package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.service.kvm.KVMOperateEndService;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadI;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadO;
import com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo.EAPSyncEqpInfoI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportIA;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardI;
import com.totainfo.eap.cp.trx.kvm.EAPEndCard.EAPEndCardO;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInI;
import com.totainfo.eap.cp.trx.kvm.EAPLotInfoWriteIn.EAPLotInfoWriteInO;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportI;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.*;
import java.time.format.DateTimeFormatter;
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
            EapEndCard(evtNo);
            Remove(evtNo);
            return;
        }
        //发送给前端，LOT 校验成功。
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]:批次号:[" + lotId + "]探针:["+ proberId +"] MES 验证成功。");
        Stateset("1","2",lotId);


//       //时间校验功能接口
//        KVMTimeReportI kvmTimeReportI = new KVMTimeReportI();
//        kvmTimeReportI.setTrxId("EAPACCEPT");
//        kvmTimeReportI.setActionFlg("TIME");
//        kvmTimeReportI.setEqpId(equipmentNo);
//        String returnMsg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmTimeReportI);
//        if(StringUtils.isEmpty(returnMsg)){
//            outTrx.setRtnCode(KVM_TIME_OUT);
//            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报指令，KVM没有回复");
//            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
//            return;
//        }
//        KVMTimeReportO kvmTimeReportO = JacksonUtils.string2Object(returnMsg, KVMTimeReportO.class);
//        if(!RETURN_CODE_OK.equals(kvmTimeReportO.getRtnCode())){
//            outTrx.setRtnCode(kvmTimeReportO.getRtnCode());
//            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报信息，KVM返回失败，原因:[" + kvmTimeReportO.getRtnMesg() + "]");
//            Remove(evtNo);
//            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
//        }
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
//        String eqpTimeNow = kvmTimeReportO.getOpeContent();
//        LogUtils.info("机台上报时间[{}]",eqpTimeNow);
//        LocalTime eqpTime = LocalTime.parse(eqpTimeNow, formatter);
//        // 得到当前的北京时间
//        ZonedDateTime beijingTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
//        LocalTime localTimeNow = beijingTime.toLocalTime();
//        LogUtils.info("北京时间是[{}]",localTimeNow);
//        Duration duration = Duration.between(eqpTime, localTimeNow);
//        long differenceInMinutes = duration.toMinutes();
//        // 检查时间是否相差五分钟或以上
//        if (differenceInMinutes >= 5 || differenceInMinutes <= -5) {
//             ClientHandler.sendMessage(evtNo,true,1,"机台时间[" + eqpTime + "]与北京时间[" + localTimeNow +"]相差五分钟以上，请检查");
//             return;
//        }

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
            EapEndCard(evtNo);
            Remove(evtNo);
            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
        }
        //发送给前端，LOT信息发送KVM成功
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]:批次:[" + lotId + "]信息下发KVM成功。");
        MesHandler.eqptStatReport(evtNo, GenergicStatDef.EqptStat.RUN,"无",lotInfo.getUserId());
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
}
