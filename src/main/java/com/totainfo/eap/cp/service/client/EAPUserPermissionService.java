package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPUserPermission.EAPUserPermissionI;
import com.totainfo.eap.cp.trx.client.EAPUserPermission.EAPUserPermissionO;
import com.totainfo.eap.cp.trx.mes.EAPReqUserAuthority.EAPReqUserAuthorityO;
import com.totainfo.eap.cp.util.LogUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author WangShuai
 * @date 2024/3/7
 */
@Service("EAPUSER")
public class EAPUserPermissionService extends EapBaseService<EAPUserPermissionI, EAPUserPermissionO>{
    @Override
    public void mainProc(String evtNo, EAPUserPermissionI inTrx, EAPUserPermissionO outTrx) {

        String userId = inTrx.getUserId();
        String passWord = inTrx.getPassWord();
        String trxId = inTrx.getTrxId();
        String actionFlg = inTrx.getActionFlg();

        if (StringUtils.isEmpty(userId)){
            outTrx.setRtnCode("00000001");
            outTrx.setRtnCode("用户名不能为空！！！");
            return;
        }

        EAPReqUserAuthorityO eapReqUserAuthorityO = MesHandler.userAuth(evtNo, userId);
        if (!RETURN_CODE_OK.equals(eapReqUserAuthorityO.getRtnCode())) {
            EAPUserPermissionO eapUserPermissionO = new EAPUserPermissionO();
            eapUserPermissionO.setTrxId(trxId);
            eapUserPermissionO.setActionFlg(actionFlg);
            eapUserPermissionO.setRtnCode(eapReqUserAuthorityO.getRtnCode());
            eapUserPermissionO.setRtnMesg(eapReqUserAuthorityO.getRtnMesg());
            ClientHandler.sendUserHandk(evtNo,eapUserPermissionO);
        }else {
            EAPUserPermissionO eapUserPermissionO = new EAPUserPermissionO();
            eapUserPermissionO.setTrxId(trxId);
            eapUserPermissionO.setActionFlg(actionFlg);
            eapUserPermissionO.setRtnCode(eapReqUserAuthorityO.getRtnCode());
            eapUserPermissionO.setRtnMesg(eapReqUserAuthorityO.getRtnMesg());
            ClientHandler.sendUserHandk(evtNo,eapUserPermissionO);
        }
    }
}