package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;

import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptStat;
import com.totainfo.eap.cp.commdef.GenergicStatDef.StepName;
import com.totainfo.eap.cp.commdef.GenergicStatDef.StepStat;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadI;
import com.totainfo.eap.cp.trx.client.EAPLotIdRead.EAPLotIdReadO;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportI;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportIA;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportO;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportI;
import com.totainfo.eap.cp.trx.kvm.KVMTimeReport.KVMTimeReportO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOA;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.*;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.*;
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
    private IEqptDao eqptDao;

    @Resource
    private IStateDao stateDao;

    @Resource
    private HttpHandler httpHandler;

    @Resource
    private ClientHandler clientHandler;

    @Override
    public void mainProc(String evtNo, EAPLotIdReadI inTrx, EAPLotIdReadO outTrx){
        StateInfo stateInfo = new StateInfo();
        stateInfo.setStep(StepName.FIRST);
        stateInfo.setState(StepStat.INPROCESS);

        mainProc2(evtNo, stateInfo, inTrx, outTrx);
        if(!RETURN_CODE_OK.equals(outTrx.getRtnCode())){
            stateInfo.setState(StepStat.FAIL);
            stateDao.addStateInfo(stateInfo);
        }
    }


    public void mainProc2(String evtNo, StateInfo stateInfo, EAPLotIdReadI inTrx, EAPLotIdReadO outTrx) {
        String lotId = inTrx.getLotNo();
        stateInfo.setLotNo(lotId);
        stateDao.addStateInfo(stateInfo);
        String datas = inTrx.getDatas();
        int selectType = inTrx.getSelectType();

        if(StringUtils.isEmpty(lotId)){
            outTrx.setRtnCode(LOT_ID_EMPTY);
            outTrx.setRtnMesg("批次号为空，请重新扫描!");
            return;
        }
        String proberId = inTrx.getProberCardId();
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

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo != null){
            outTrx.setRtnCode(LOT_INFO_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:批次:[" + lotInfo.getLotId() + "]制程未结束，请等待");
            return;
        }


        String sampleValue = null;
        EAPReqLotInfoO eapReqLotInfoO = MesHandler.lotInfoReq(evtNo, lotId, proberId, userId);
        if(!RETURN_CODE_OK.equals(eapReqLotInfoO.getRtnCode())){
            outTrx.setRtnCode(LOT_INFO_EXIST);
            outTrx.setRtnMesg("[" + eapReqLotInfoO.getRtnMesg() + "]");
            return;
        }

        EAPReqLotInfoOC eapReqLotInfoOC;
        for (EAPReqLotInfoOB eapReqLotInfoOB : eapReqLotInfoO.getLotInfo().getParamList()){
            if ("Sample".equals(eapReqLotInfoOB.getParamName())){
                sampleValue = eapReqLotInfoOB.getParamValue();
            }
        }

        eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
        if (selectType != Integer.valueOf(eapReqLotInfoOC.getType())){
            LogUtils.info("Client 下发的数据为：[" +inTrx.getUserId() + inTrx.getProberCardId() + inTrx.getLotNo() + inTrx.getSelectType() + inTrx.getDatas() + "]");
            eapReqLotInfoOC.setDatas(datas);
            eapReqLotInfoOC.setType(String.valueOf(inTrx.getSelectType()));
            String sampleClientValue = JacksonUtils.object2String(eapReqLotInfoOC);
            EAPReqLotInfoOB reqLotInfoOB = new EAPReqLotInfoOB();
            reqLotInfoOB.setParamName("Sample");
            reqLotInfoOB.setParamValue(sampleClientValue);
            LogUtils.info("存入Redis中的数据为：[" + reqLotInfoOB.getParamName() + reqLotInfoOB.getParamValue() + "]");
            lotDao.addClientLotInfo(reqLotInfoOB);
        }

        //发送给前端，LOT 校验成功。
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]: 批次号:[" + lotId + "]探针:["+ proberId +"]MES帐料信息验证成功");

        EAPReqLotInfoOA eapReqLotInfoOA = eapReqLotInfoO.getLotInfo();
        lotInfo = new LotInfo();
        lotInfo.setLotId(lotId);
        lotInfo.setWaferLot(eapReqLotInfoOA.getWaferLot());
        lotInfo.setDevice(eapReqLotInfoOA.getDevice());
        lotInfo.setLoadBoardId(eapReqLotInfoOA.getLoadBoardId());
        lotInfo.setProberCard(proberId);
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
        List<EMSDeviceParameterReportIA>  emsDeviceParameterReportIAS = new ArrayList<>(2);

        EMSDeviceParameterReportIA emsDeviceParameterReportIA = new EMSDeviceParameterReportIA();
        emsDeviceParameterReportIA.setParamCode("1");
        emsDeviceParameterReportIA.setParamName("温度");
        emsDeviceParameterReportIA.setParamValue(tempValue);
        emsDeviceParameterReportIAS.add(emsDeviceParameterReportIA);

        emsDeviceParameterReportIA = new EMSDeviceParameterReportIA();
        emsDeviceParameterReportIA.setParamCode("2");
        emsDeviceParameterReportIA.setParamName("温度范围");
        emsDeviceParameterReportIA.setParamValue(lotInfo.getTemperatureRange());
        emsDeviceParameterReportIAS.add(emsDeviceParameterReportIA);
        emsDeviceParameterReportI.setParamList(emsDeviceParameterReportIAS);

        EmsHandler.emsDeviceParameterReportToEms(evtNo,lotId,emsDeviceParameterReportI);
        ClientHandler.sendMessage(evtNo,false,2,"[EAP-EMS]:EAP给EMS上报设备参数信息指令成功");
        EmsHandler.reportRunWorkInfo(evtNo,"上报Lot信息",lotId,"","OK","Success", Thread.currentThread().getStackTrace()[1].getMethodName());
        LogUtils.info("开始执行上报Lot数据结束方法");
        //第一步Lot信息上报结束
        clientHandler.setFlowStep(StepName.FIRST,StepStat.COMP);
        // 时间校验功能接口
        KVMTimeReportI kvmTimeReportI = new KVMTimeReportI();
        kvmTimeReportI.setTrxId("EAPACCEPT");
        kvmTimeReportI.setActionFlg("TIME");
        kvmTimeReportI.setEqpId(equipmentNo);
        String returnMsg = httpHandler.postHttpForEqpt(evtNo, proberUrl, kvmTimeReportI);
        if(StringUtils.isEmpty(returnMsg)){
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报指令，KVM没有回复");
            return;
        }
        KVMTimeReportO kvmTimeReportO = JacksonUtils.string2Object(returnMsg, KVMTimeReportO.class);
        if(!RETURN_CODE_OK.equals(kvmTimeReportO.getRtnCode())){
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            outTrx.setRtnCode(kvmTimeReportO.getRtnCode());
            outTrx.setRtnMesg("[EAP-KVM]:EAP下发请求时间上报信息，KVM返回失败，原因:[" + kvmTimeReportO.getRtnMesg() + "]");
            return;
        }
        ClientHandler.sendMessage(evtNo,false,2,"EAP开始进行时间校验, 等待验证结果");
    }


    public void removeCache(){
        lotDao.removeLotInfo();
        stateDao.removeState();
    }
}
