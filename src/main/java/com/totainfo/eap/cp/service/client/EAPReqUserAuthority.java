package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicCodeDef;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.mes.EAPReqUserAuthority.EAPReqUserAuthorityI;
import com.totainfo.eap.cp.trx.mes.EAPReqUserAuthority.EAPReqUserAuthorityO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.LOT_INFO_NOT_EXIST;

@Service("ReqUserAuthor")
public class EAPReqUserAuthority extends EapBaseService<EAPReqUserAuthorityI, EAPReqUserAuthorityO> {
    @Resource
    private ILotDao lotDao;

    @Override
    public void mainProc(String evtNo, EAPReqUserAuthorityI inTrx, EAPReqUserAuthorityO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if (lotInfo == null) {
            outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
            outTrx.setRtnMesg("[EAP-Client]:没有找到需要制程的批次信息，请确认");
            ClientHandler.sendMessage(evtNo, false, 1, outTrx.getRtnMesg());
            return;
        }
        String userId = lotInfo.getUserId();
        EAPReqUserAuthorityO eapReqUserAuthorityO = MesHandler.userAuth(evtNo, userId);
        if (!GenergicStatDef.Constant.RETURN_CODE_OK.equals(eapReqUserAuthorityO.getRtnCode())) {
            outTrx.setRtnCode(eapReqUserAuthorityO.getRtnCode());
            outTrx.setRtnMesg(eapReqUserAuthorityO.getRtnMesg());
            ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
            return;
        }
        ClientHandler.sendMessage(evtNo,false,2,"该人员手动输入权限校验通过，请输入相关信息");
    }
}
