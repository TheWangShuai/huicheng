package com.totainfo.eap.cp.service.client;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.HttpHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlI;
import com.totainfo.eap.cp.trx.client.EAPEqpControl.EAPEqpControlO;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandI;
import com.totainfo.eap.cp.trx.kvm.EAPControlCommand.EAPControlCommandO;
import com.totainfo.eap.cp.trx.mes.EAPReqCheckIn.EAPReqCheckInO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.totainfo.eap.cp.commdef.GenergicCodeDef.KVM_TIME_OUT;
import static com.totainfo.eap.cp.commdef.GenergicCodeDef.LOT_INFO_NOT_EXIST;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:34
 */
@Service("StartOrStop")
public class EAPEqpControlService extends EapBaseService<EAPEqpControlI, EAPEqpControlO> {

    @Resource
    private ILotDao lotDao;

    @Resource
    private HttpHandler httpHandler;

    @Override
    public void mainProc(String evtNo, EAPEqpControlI inTrx, EAPEqpControlO outTrx) {
        String userId = inTrx.getUserId();
        String model = inTrx.getModel();
        boolean isCheckIn = inTrx.getIsCheckIn();

        if(isCheckIn){
            LotInfo lotInfo =lotDao.getCurLotInfo();
            if(lotInfo == null){
                outTrx.setRtnCode(LOT_INFO_NOT_EXIST);
                outTrx.setRtnMesg("没有找需要制程的批次信息，请确认");
                return;
            }

            EAPReqCheckInO eapReqCheckInO = MesHandler.checkInReq(evtNo, lotInfo.getLotId(), userId);
            if(!RETURN_CODE_OK.equals(eapReqCheckInO.getRtnCode())){
                outTrx.setRtnCode(eapReqCheckInO.getRtnCode());
                outTrx.setRtnMesg(eapReqCheckInO.getRtnMesg());
                return;
            }
            ClientHandler.sendMessage(evtNo, false, 2, "批次:[" + lotInfo.getLotId() + "] Check In 成功。");
        }

        EAPControlCommandI eapControlCommandI = new EAPControlCommandI();
        eapControlCommandI.setTrxId("EAPACCEPT");
        eapControlCommandI.setActionFlg("RJPI");
        eapControlCommandI.setUserId(userId);
        eapControlCommandI.setModel(model);

        String returnMsg = httpHandler.postHttpForEqpt(evtNo, GenericDataDef.testerUrl, eapControlCommandI);
        if(StringUtils.isEmpty(returnMsg)){
            outTrx.setRtnCode(KVM_TIME_OUT);
            outTrx.setRtnMesg("EAP 发送设备启停指令，KVM 没有回复");
            return;
        }
        EAPControlCommandO eapControlCommandO = JacksonUtils.string2Object(returnMsg, EAPControlCommandO.class);
        if(!RETURN_CODE_OK.equals(eapControlCommandO.getRtnCode())){
            outTrx.setRtnCode(eapControlCommandO.getRtnCode());
            outTrx.setRtnMesg("EAP 发送设备启停指令，KVM 返回失败，原因:[" + eapControlCommandO.getRtnMesg() + "]");
            return;
        }

        String statDes = "0".equals(model)? "启动":"停止";
        ClientHandler.sendMessage(evtNo, false, 2, "EAP 发送设备启停指令成功，当前状态:[" + statDes + "]");
    }
}
