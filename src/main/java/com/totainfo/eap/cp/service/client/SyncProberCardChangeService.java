package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPSyncProberCardChange.EAPSyncProberCardChangeI;
import com.totainfo.eap.cp.trx.client.EAPSyncProberCardChange.EAPSyncProberCardChangeO;
import com.totainfo.eap.cp.trx.mes.EAPSyncProberCard.MESSyncProberCardO;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.PROBER_ID_EMPTY;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.USER_ID_EMPTY;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:09
 */
@Service("ProberCardChange")
public class SyncProberCardChangeService extends EapBaseService<EAPSyncProberCardChangeI, EAPSyncProberCardChangeO> {

    @Override
    public void mainProc(String evtNo, EAPSyncProberCardChangeI inTrx, EAPSyncProberCardChangeO outTrx) {
        String proberId = inTrx.getProberCardId();
        if(StringUtils.isEmpty(proberId)){
            outTrx.setRtnCode(PROBER_ID_EMPTY);
            outTrx.setRtnMesg("探针号为空，请重新扫描!");
            return;
        }
        String userId = inTrx.getUserId();
        if(StringUtils.isEmpty(userId)){
            outTrx.setRtnCode(USER_ID_EMPTY);
            outTrx.setRtnMesg("操作员ID为空，请输入!");
            return;
        }
        MESSyncProberCardO mesSyncProberCardO = MesHandler.syncProberCardInfo(evtNo, userId, proberId);
        if(RETURN_CODE_OK.equals(mesSyncProberCardO.getRtnCode())){
            outTrx.setRtnCode(mesSyncProberCardO.getRtnCode());
            outTrx.setRtnMesg(mesSyncProberCardO.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo, false, 2, "EAP同步探针：["+ proberId + "]信息完成。");
    }
}
