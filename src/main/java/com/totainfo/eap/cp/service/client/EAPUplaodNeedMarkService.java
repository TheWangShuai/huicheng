package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkI;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkO;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultO;
import org.springframework.stereotype.Service;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:20
 */
@Service("UploadNeedleMark")
public class EAPUplaodNeedMarkService  extends EapBaseService<EAPUploadNeedMarkI, EAPUploadNeedMarkO> {
    @Override
    public void mainProc(String evtNo, EAPUploadNeedMarkI inTrx, EAPUploadNeedMarkO outTrx) {
         String userId = inTrx.getUserId();
         String lotId = inTrx.getLotNo();
         String waferId = inTrx.getWaferId();
         String coordinate = inTrx.getCoordinate();
         String result = inTrx.getResult();
         String remark = inTrx.getRemark();


        EAPUploadMarkResultO eapUploadMarkResultO  = MesHandler.uploadMarkResult(evtNo, lotId, waferId,coordinate, result,remark, userId);
        if(!RETURN_CODE_OK.equals(eapUploadMarkResultO.getRtnCode())){
            outTrx.setRtnCode(eapUploadMarkResultO.getRtnCode());
            outTrx.setRtnMesg(eapUploadMarkResultO.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP 上传针痕信息成功。");
    }
}
