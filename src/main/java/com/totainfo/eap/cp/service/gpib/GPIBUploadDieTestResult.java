package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.ems.EMSGetTestResult.EMSGetTestResultO;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultI;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultIA;
import com.totainfo.eap.cp.trx.gpib.GPIBUploadDieTestResult.GPIBUploadDieTestResultO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

@Service("uploaddietestresultReport")
public class GPIBUploadDieTestResult extends EapBaseService <GPIBUploadDieTestResultI , GPIBUploadDieTestResultO> {

    @Resource
    private ILotDao lotDao;
    @Override
    public void mainProc(String evtNo, GPIBUploadDieTestResultI inTrx, GPIBUploadDieTestResultO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String userId = lotInfo.getUserId();
        String lotNo = inTrx.getLotNo();
        String waferId = inTrx.getWaferId();
        List<GPIBUploadDieTestResultIA> datas = inTrx.getDatas();

        //TODO 取到的卡控值 后续在加入判断
        EMSGetTestResultO testResult = EmsHandler.getTestResult(evtNo, lotInfo.getDevice());
        String itemName = testResult.getItemList().getItemName();
        String itemEnable = testResult.getItemList().getItemEnable();
        String itemValue = testResult.getItemList().getItemValue();

//        String string = datas.toString();
//        GPIBUploadDieTestResultIA gpibUploadDieTestResultIA = JacksonUtils.string2Object(string, GPIBUploadDieTestResultIA.class);
//        String result = gpibUploadDieTestResultIA.getResult();
//        String startCoorDinates = gpibUploadDieTestResultIA.getStartCoorDinates();
        EAPUploadDieResultO eapUploadDieResultO = MesHandler.uploadDieResult(evtNo, lotNo, waferId, datas, userId);
        if (!RETURN_CODE_OK.equals(eapUploadDieResultO.getRtnCode())) {
            outTrx.setRtnCode(eapUploadDieResultO.getRtnCode());
            outTrx.setRtnMesg(eapUploadDieResultO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] WaferStart时间上报成功");
    }
    }

