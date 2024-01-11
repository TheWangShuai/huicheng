package com.totainfo.eap.cp.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkI;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkIA;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkO;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultI;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:20
 */
@Service("UploadNeedleMark")
public class EAPUplaodNeedMarkService extends EapBaseService<EAPUploadNeedMarkI, EAPUploadNeedMarkO> {
    @Override
    public void mainProc(String evtNo, EAPUploadNeedMarkI inTrx, EAPUploadNeedMarkO outTrx) {
        String userId = inTrx.getUserId();
        String lotId = inTrx.getLotNo();
        String waferId = inTrx.getWaferId();
        String startCoordinate = inTrx.getCoordinate();
        String result = inTrx.getResult();
        String remark = inTrx.getRemark();
        List<EAPUploadNeedMarkIA> datas= new ArrayList<>();

        EAPUploadNeedMarkIA eapUploadNeedMarkIA = new EAPUploadNeedMarkIA();
        eapUploadNeedMarkIA.setStartCoordinate(startCoordinate);
        eapUploadNeedMarkIA.setResult(result);
        datas.add(eapUploadNeedMarkIA);


        EAPUploadMarkResultO eapUploadMarkResultO = MesHandler.uploadMarkResult(evtNo, lotId, waferId, remark, datas,userId);
        if (!RETURN_CODE_OK.equals(eapUploadMarkResultO.getRtnCode())) {
            outTrx.setRtnCode(eapUploadMarkResultO.getRtnCode());
            outTrx.setRtnMesg(eapUploadMarkResultO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]:EAP 上传针痕信息成功。");
    }


}
