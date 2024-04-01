package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPWaferLocation.EAPWaferLocationI;
import com.totainfo.eap.cp.trx.client.EAPWaferLocation.EAPWaferLocationO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.*;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author WangShuai
 * @date 2024/3/22
 * 新增返回SlotMap信息到Client端接口
 */
@Service("GETLOTINFO")
public class EAPReqWaferLocation extends EapBaseService<EAPWaferLocationI, EAPWaferLocationO> {


    @Override
    public void mainProc(String evtNo, EAPWaferLocationI inTrx, EAPWaferLocationO outTrx) {

        String lotNo = inTrx.getLotNo();
        String proberCardId = inTrx.getProberCardId();
        String userId = inTrx.getUserId();

        if(StringUtils.isEmpty(lotNo)){
            outTrx.setRtnCode(LOT_ID_EMPTY);
            outTrx.setRtnMesg("批次号为空,请确认!");
            return;
        }
        if(StringUtils.isEmpty(proberCardId)){
            outTrx.setRtnCode(PROBER_ID_EMPTY);
            outTrx.setRtnMesg("探针号为空，请确认!");
            return;
        }

        if(StringUtils.isEmpty(userId)){
            outTrx.setRtnCode(USER_ID_EMPTY);
            outTrx.setRtnMesg("操作员ID为空，请确认!");
            return;
        }

       EAPReqLotInfoO eapReqLotInfoO = MesHandler.lotInfoReq(evtNo, lotNo, proberCardId, userId);
        if(!RETURN_CODE_OK.equals(eapReqLotInfoO.getRtnCode())){
            outTrx.setRtnCode(LOT_INFO_EXIST);
            outTrx.setRtnMesg("[" + eapReqLotInfoO.getRtnMesg() + "]");
            return;
        }
        String sampleValue = null;

        for (EAPReqLotInfoOB eapReqLotInfoOB : eapReqLotInfoO.getLotInfo().getParamList()){
            if ("Sample".equals(eapReqLotInfoOB.getParamName())){
                sampleValue = eapReqLotInfoOB.getParamValue();
            }
        }

        // 给EAP Client端下发MES返回的SlotMap数据
        ClientHandler.waferSlotReport(evtNo,eapReqLotInfoO.getRtnCode(),eapReqLotInfoO.getRtnMesg(),sampleValue);
    }
}