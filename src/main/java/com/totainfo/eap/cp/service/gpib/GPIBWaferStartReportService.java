package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenergicStatDef.MessageType;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.*;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GBIPWaferEndReport.GPIBWaferEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportInfoO;
import com.totainfo.eap.cp.util.FtpUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("waferStartReport")
public class GPIBWaferStartReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {

    @Resource
    private ILotDao lotDao;

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

    @Override
    public void mainProc(String evtNo, GPIBWaferStartReportI inTrx, GPIBWaferStartReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }
        String lotNo =  lotInfo.getLotId();
        String evtUsr = lotInfo.getUserId();
        String waferId = inTrx.getWaferId();
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上传生产Wafer信息指令成功");
        EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo,lotNo, waferId, "Start");
        if (!RETURN_CODE_OK.equals(emsWaferReportO.getRtnCode())){
            ClientHandler.sendMessage(evtNo, false, 2, emsWaferReportO.getRtnMesg());
        }

        GPIBWaferStartReportO gpibWaferStartReportO = MesHandler.waferStart(evtNo, evtUsr, lotNo, waferId);
        if (!RETURN_CODE_OK.equals(gpibWaferStartReportO.getRtnCode())) {
            ClientHandler.sendMessage(evtNo, false, 1 , gpibWaferStartReportO.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[" + waferId + "] : 测试开始 ");
    }
}

