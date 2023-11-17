package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkI;
import com.totainfo.eap.cp.trx.client.EAPUploadNeedMark.EAPUploadNeedMarkO;
import com.totainfo.eap.cp.trx.mes.EAPUploadMarkResult.EAPUploadMarkResultO;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> map = new HashMap<>();
        map.put("startCoordinates", startCoordinate);
        map.put("result", result);
        String s = map.toString();
        List<String> datas = new ArrayList<>();
        datas.add(s);


        EAPUploadMarkResultO eapUploadMarkResultO = MesHandler.uploadMarkResult(evtNo, lotId, waferId, datas, remark, userId);
        if (!RETURN_CODE_OK.equals(eapUploadMarkResultO.getRtnCode())) {
            outTrx.setRtnCode(eapUploadMarkResultO.getRtnCode());
            outTrx.setRtnMesg(eapUploadMarkResultO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "[EAP-MES]:EAP 上传针痕信息成功。");
    }
}
