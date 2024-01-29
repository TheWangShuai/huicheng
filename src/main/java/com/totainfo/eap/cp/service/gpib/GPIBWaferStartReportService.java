package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.trx.rcm.EapReportAlarmInfoI;
import com.totainfo.eap.cp.trx.rcm.EapReportAlarmInfoO;
import com.totainfo.eap.cp.util.FtpUtils;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.RCM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("waferStartReport")
public class GPIBWaferStartReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {
    @Resource
    private ILotDao lotDao;

    @Resource
    private HttpHandler httpHandler;

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
        String pvWaferId = inTrx.getPvWaferId();
        //TODO 从gpib获取waferState
        EMSWaferReportI emsWaferReportI = new EMSWaferReportI();
        emsWaferReportI.setLotNo(lotNo);
        emsWaferReportI.setWaferNo(waferId);

        //上报给RCM有关wafer的信息
//        EapReportAlarmInfoI eapReportAlarmInfoI = new EapReportAlarmInfoI();
//        eapReportAlarmInfoI.setTrxId("eapReportAlarmInfo");
//        eapReportAlarmInfoI.setEquipmentNo(GenericDataDef.equipmentNo);
//        eapReportAlarmInfoI.setEquipmentState(GenergicStatDef.EqptStat.RUN);
//        eapReportAlarmInfoI.setLotId(lotInfo.getLotId());
//        eapReportAlarmInfoI.setWaferDates(waferId);
//        String returnMsgFromRcm = httpHandler.postHttpForRcm(evtNo, GenericDataDef.rcmUrl, eapReportAlarmInfoI);
//        if (StringUtils.isEmpty(returnMsgFromRcm)){
//            outTrx.setRtnCode(RCM_TIME_OUT);
//            outTrx.setRtnMesg("[EAP-RCM]:EAP上报批次信息，RCM没有回复");
//            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
//            return;
//        }
//        EapReportAlarmInfoO eapReportAlarmInfoO = JacksonUtils.string2Object(returnMsgFromRcm, EapReportAlarmInfoO.class);
//        if(!RETURN_CODE_OK.equals(eapReportAlarmInfoO.getRtnCode())){
//            outTrx.setRtnCode(eapReportAlarmInfoO.getRtnCode());
//            outTrx.setRtnMesg("[EAP-RCM]:EAP上报批次信息，RCM返回失败，原因:[" + eapReportAlarmInfoO.getRtnMesg() + "]");
//            ClientHandler.sendMessage(evtNo,false,1,outTrx.getRtnMesg());
//        }


        //Wafer Start时判断，上一片Wafer Die数据是否上报完成，如果没有，将上一片Wafer的Die数据上报完成
        if(StringUtils.isNotEmpty(pvWaferId)){
            Map<String, List<DielInfo>> waferDieMap = lotInfo.getWaferDieMap();
            if(waferDieMap != null){
                List<DielInfo> dielInfos = waferDieMap.get(pvWaferId);
                if(dielInfos != null && !dielInfos.isEmpty()){
                    EAPUploadDieResultO eapUploadDieResultO = MesHandler.uploadDieResult(evtNo, lotNo, waferId, dielInfos, evtUsr);
                    if (!RETURN_CODE_OK.equals(eapUploadDieResultO.getRtnCode())) {
                        outTrx.setRtnCode(eapUploadDieResultO.getRtnCode());
                        outTrx.setRtnMesg(eapUploadDieResultO.getRtnMesg());
                        ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    }
                    EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo,"End", emsWaferReportI);
                    if (!RETURN_CODE_OK.equals(emsWaferReportO.getRtnCode())){
                        outTrx.setRtnCode(emsWaferReportO.getRtnCode());
                        outTrx.setRtnMesg(emsWaferReportO.getRtnMesg());
                        ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    }
                }
                waferDieMap.remove(pvWaferId);
                lotDao.addLotInfo(lotInfo);
            }
            Path dataPath = Paths.get(System.getProperty("user.dir"),"data", pvWaferId + ".XTR");
            if (!Files.exists(dataPath.getParent())){
                try {
                    Files.createDirectories(dataPath.getParent());
                } catch (IOException e) {
                    LogUtils.error("文件夹创建失败");
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("LotID=").append(lotInfo.getLotId()).append("\n")
                    .append("TestProgram=").append(lotInfo.getTestProgram()).append("\n")
                    .append("P/C=").append(lotInfo.getProberCard()).append("\n")
                    .append("touchdown=").append(lotInfo.getDieCount()).append("\n")
                    .append("OPID=").append(lotInfo.getUserId()).append("\n")
                    .append("ChuckTemp=").append(lotInfo.getTemperature()).append("\n")
                    .append("ProberName=").append(proberName).append("\n")
                    .append("TSID=").append(tsId).append("\n");
            try {
                Files.write(dataPath, sb.toString().getBytes());
                FtpUtils.uploadFile(host, user, password, port, path,dataPath.toString());
                Files.delete(dataPath);
            } catch (IOException e) {
                LogUtils.error("xtr文件写入失败");
            }
        }


        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-EMS]:EAP给EMS上传生产Wafer信息指令成功");
        EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo,"Start", emsWaferReportI);
        if (!RETURN_CODE_OK.equals(emsWaferReportO.getRtnCode())){
            outTrx.setRtnCode(emsWaferReportO.getRtnCode());
            outTrx.setRtnMesg(emsWaferReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
        }
        GPIBWaferStartReportO gpibWaferStartReportO = MesHandler.waferStart(evtNo, evtUsr, lotNo, waferId);
        if (!RETURN_CODE_OK.equals(gpibWaferStartReportO.getRtnCode())) {
            outTrx.setRtnCode(gpibWaferStartReportO.getRtnCode());
            outTrx.setRtnMesg(gpibWaferStartReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] WaferStart时间上报成功");
    }
}

