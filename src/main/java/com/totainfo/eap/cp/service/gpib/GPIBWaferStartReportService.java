package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenergicStatDef.MessageType;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.client.EAPReportDieInfo.DieInfoOA;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GBIPWaferEndReport.GPIBWaferEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.util.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("waferStartReport")
public class GPIBWaferStartReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {

    @Resource
    private ILotDao lotDao;
    @Resource
    private StateDao stateDao;

    @Value("${equipment.id}")
    private String proberName;

    @Value("${equipment.tsId}")
    private String tsId;

    @Value("${ftp.host}")
    private String host;
    @Value("${ftp.port}")
    private int port;
    @Value("${ftp.user}")
    private String user;
    @Value("${ftp.password}")
    private String password;
    @Value("${ftp.path}")
    private String path;
    private static Queue<String> slotMapQueue = new LinkedList<String>();

    private List<DieInfoOA> dieInfoOAS = new ArrayList<>();

    @Override
    public void mainProc(String evtNo, GPIBWaferStartReportI inTrx, GPIBWaferStartReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        DieCountInfo dieCountInfo = new DieCountInfo();
        DieInfoOA dieInfoOA = new  DieInfoOA();
        if(lotInfo == null){
            return;
        }
        String lotNo =  lotInfo.getLotId();
        String evtUsr = lotInfo.getUserId();
        String waferId = inTrx.getWaferId();
        String currentDate = DateUtils.getcurrentTimestampStr("yyyy-MM-dd HH:mm:ss");
        dieInfoOA.setWaferStartTime(currentDate);
        dieInfoOA.setWorkId(waferId);
        dieInfoOA.setDeviceName(lotInfo.getDeviceId());
        dieInfoOAS.add(dieInfoOA);
        dieCountInfo.setDieInfoOAS(dieInfoOAS);
        lotDao.addDieCount(dieCountInfo);
        ClientHandler.sendMessage(evtNo, false, 2, "[Prober-EAP]：WaferId: [" + waferId + "]测试开始 ");

        if (!dieCountInfo.getDieInfoOAS().isEmpty()){
            if (StringUtils.isNotEmpty(stateDao.getStateInfo().getStep()) && Integer.parseInt(stateDao.getStateInfo().getStep()) > 9 ){
                LogUtils.info("流程当前步骤为：：[" + stateDao.getStateInfo().getStep()  + "]");
                String cleanFlg = "N";
                CleanFuncKeyO cleanFuncKeyO = KvmHandler.cleanFuncKey(evtNo, cleanFlg);
                if ("0000000".equals(cleanFuncKeyO.getRtnCode())) {
                    ClientHandler.sendMessage(evtNo, false, 2, "[EAP-Tester]: EAP给测试机下发清除FunctionKey成功！");
                }
            }
        }

//        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]：上报 WaferId: [" + waferId + "]进行刻号比对,  等待结果");
//        GPIBWaferStartReportO gpibWaferStartReportO = MesHandler.waferStart(evtNo, evtUsr, lotNo, waferId);
//        if (!RETURN_CODE_OK.equals(gpibWaferStartReportO.getRtnCode())) {
//            ClientHandler.sendMessage(evtNo, false, 1 , gpibWaferStartReportO.getRtnMesg());
//            KvmHandler.haltStop(evtNo);
//            return;
//        }
//        ClientHandler.sendMessage(evtNo, false, 2, "[MES-EAP]: WaferId: [" + waferId + "]刻号比对成功   ");
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]: 批次号:[" + lotNo + "], 上报EMS批次信息");
        EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo,lotNo, waferId, "Start");
        if (!RETURN_CODE_OK.equals(emsWaferReportO.getRtnCode())){
            //给EMS上报制程结束信号
            EmsHandler.waferInfotoEms(evtNo,lotInfo.getLotId(),lotInfo.getWaferLot(), "End");
            removeCache();
            ClientHandler.sendMessage(evtNo, false, 2, emsWaferReportO.getRtnMesg());
        }
    }

    public void removeCache(){
        lotDao.removeLotInfo();
        stateDao.removeState();
    }
}

