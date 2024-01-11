package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportI;
import com.totainfo.eap.cp.trx.ems.EMSWaferReport.EMSWaferReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("WaferStartReport")
public class GPIBWaferStartReportService extends EapBaseService<GPIBWaferStartReportI, GPIBWaferStartReportO> {
    @Resource
    private ILotDao lotDao;


    @Override
    public void mainProc(String evtNo, GPIBWaferStartReportI inTrx, GPIBWaferStartReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String evtUsr = lotInfo.getUserId();
//        String evtUsr = inTrx.getEvtUsr();
        String waferId = inTrx.getWaferId();
        String lotNo = inTrx.getLotNo();

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

