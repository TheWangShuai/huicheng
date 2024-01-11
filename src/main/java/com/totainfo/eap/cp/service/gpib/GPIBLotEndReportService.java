package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSLotinfoReport.EMSLotInfoReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportO;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("LotEndReport")
public class GPIBLotEndReportService extends EapBaseService<GPIBLotEndReportI, GPIBLotEndReportO> {
    @Resource
    private ILotDao lotDao;

    @Override
    public void mainProc(String evtNo, GPIBLotEndReportI inTrx, GPIBLotEndReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String evtUsr = lotInfo.getUserId();
        String lotNo = inTrx.getLotNo();

        EMSLotInfoReportI emsLotInfoReportI = new EMSLotInfoReportI();
        emsLotInfoReportI.setLotNo(lotInfo.getLotId());
        emsLotInfoReportI.setDeviceName(lotInfo.getDevice());
        emsLotInfoReportI.setEquipmentNo(GenericDataDef.equipmentNo);
        emsLotInfoReportI.setTestProgram(lotInfo.getTestProgram());
        emsLotInfoReportI.setProberCard(lotInfo.getProberCard());
        emsLotInfoReportI.setProcessState("4");
        emsLotInfoReportI.setOperator(lotInfo.getUserId());
        emsLotInfoReportI.setTemperature(lotInfo.getTemperature());
        EmsHandler.emsLotInfoReporToEms(evtNo,emsLotInfoReportI);

        GPIBLotEndReportO gpibLotEndReportO = MesHandler.lotEnd(evtNo, evtUsr, lotNo);
        if (!RETURN_CODE_OK.equals(gpibLotEndReportO.getRtnCode())) {
            outTrx.setRtnCode(gpibLotEndReportO.getRtnCode());
            outTrx.setRtnMesg(gpibLotEndReportO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
        }
        ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] End结束时间上报成功");

    }
}

