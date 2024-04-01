package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.KvmHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GBIPWaferEndReport.GPIBWaferEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.kvm.cleanFuncKey.CleanFuncKeyO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.util.FtpUtils;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("waferEndReport")
public class GPIBWaferEndReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {

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
        // 当前片WaferID
        String waferId = inTrx.getWaferId(); //214124
        // 上一片的WaferID
        String pvWaferId = inTrx.getPvWaferId(); //214123


        //Wafer Start时判断，上一片Wafer Die数据是否上报完成，如果没有，将上一片Wafer的Die数据上报完成
//        if(StringUtils.isNotEmpty(pvWaferId)){
            Map<String, List<DielInfo>> waferDieMap = lotInfo.getWaferDieMap();
            if(waferDieMap != null){
                LogUtils.info("开始判断上一片wafer的waferDieMap是否上报完成-----");
                List<DielInfo> dielInfos = waferDieMap.get(waferId);
                if(dielInfos != null && !dielInfos.isEmpty()){
                    LogUtils.info("给MES上报waferDieMap数据-----");
                    EAPUploadDieResultO eapUploadDieResultO = MesHandler.uploadDieResult(evtNo, lotNo, waferId, dielInfos, evtUsr);
                    if (!RETURN_CODE_OK.equals(eapUploadDieResultO.getRtnCode())) {
                        outTrx.setRtnCode(eapUploadDieResultO.getRtnCode());
                        outTrx.setRtnMesg(eapUploadDieResultO.getRtnMesg());
                        ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    }

                    LogUtils.info("给EMS上报waferEnd-----");
                    EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo,lotNo,waferId, "End");
                    if (!RETURN_CODE_OK.equals(emsWaferReportO.getRtnCode())){
                        outTrx.setRtnCode(emsWaferReportO.getRtnCode());
                        outTrx.setRtnMesg(emsWaferReportO.getRtnMesg());
                        ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                    }

                    LogUtils.info("给MES上报waferEnd-----");
                    GPIBWaferEndReportO gpibWaferEndReportO = MesHandler.waferEnd(evtNo, evtUsr, lotNo, waferId);
                    if (RETURN_CODE_OK.equals(gpibWaferEndReportO.getRtnCode())) {
                        ClientHandler.sendMessage(evtNo, false, 2, "EAP给MES上报WaferEnd成功！");
                    }

                    CleanFuncKeyO cleanFuncKeyO = KvmHandler.cleanFuncKey(evtNo, "N");
                    if ("0000000".equals(cleanFuncKeyO.getRtnCode())) {
                        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-KVM]: EAP 下发获取程式成功！ ");
                    }
                }

                waferDieMap.remove(waferId);
                lotDao.addLotInfo(lotInfo);
            }
            Path dataPath = Paths.get(System.getProperty("user.dir"),"data", waferId + ".XTR");
            if (!Files.exists(dataPath.getParent())){
                try {
                    Files.createDirectories(dataPath.getParent());
                } catch (IOException e) {
                    LogUtils.error("文件夹创建失败");
                }
            }
            // 换片结束，生成对于你的文件
            LogUtils.info("换片结束，开始生成换片文件!");
            StringBuilder sb = new StringBuilder();
            sb.append("LotID=").append(lotInfo.getLotId()).append("\n")
                    .append("TestProgram=").append(lotInfo.getTestProgram()).append("\n")
                    .append("P/C=").append(lotInfo.getProberCard()).append("\n")
                    .append("touchdown=").append(lotInfo.getDieCount()).append("\n")
                    .append("OPID=").append(lotInfo.getUserId()).append("\n")
                    .append("ChuckTemp=").append(lotInfo.getTemperature()).append("\n")
                    .append("ProberName=").append(proberName).append("\n")
                    .append("TSID=").append(tsId).append("\n");
            LogUtils.info("换片生成的数据为：" + sb);
            try {
                Files.write(dataPath, sb.toString().getBytes());
                FtpUtils.uploadFile(host, user, password, port, path,dataPath.toString());
                Files.delete(dataPath);
            } catch (IOException e) {
                LogUtils.error("xtr文件写入失败");
            }
//        }

        ClientHandler.sendMessage(evtNo, false, 2, "[" + waferId + "] : 测试完成 ");
    }
}

