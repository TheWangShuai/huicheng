package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("waferStartReport")
public class GPIBWaferStartReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {
    @Resource
    private ILotDao lotDao;


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
                }
                waferDieMap.remove(pvWaferId);
                lotDao.addLotInfo(lotInfo);
            }
        }

        String waferSeq = "1";
        String comment = "1";
        EMSWaferReportI emsWaferReportI = new EMSWaferReportI();
        emsWaferReportI.setLotNo(lotNo);
        emsWaferReportI.setWaferNo(waferId);
        emsWaferReportI.setWaferSeq(waferSeq);

        EMSWaferReportO emsWaferReportO = EmsHandler.waferInfotoEms(evtNo, emsWaferReportI);
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

