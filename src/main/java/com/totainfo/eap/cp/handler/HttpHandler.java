package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
public class HttpHandler<I extends BaseTrxI> {
    @Resource
    private RestTemplate restTemplate;


    public String postHttpForEqpt(String evtNo, String url, I inObj) {
        String trxId = inObj.getTrxId();
        String trxName = inObj.getTrxName();
        String actionFlag = inObj.getActionFlg();
        String requestMesg = JacksonUtils.object2String(inObj);
        String realUrl = url + "/" + trxId + "/" + actionFlag;
        LogUtils.info("[{}][{}]:[{}]->[{}]", evtNo,"EAP->KVM", trxId, requestMesg);
        String returnMesg = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("traceId", evtNo);
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Content-Type", "application/json;charset=UTF-8");
            try {
                Thread.sleep(500); // 暂停0.5秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ResponseEntity<String> exchange = restTemplate.exchange(realUrl, HttpMethod.POST, new HttpEntity<>(requestMesg, headers), String.class);
            returnMesg = exchange.getBody();
        } catch (Exception e) {
            LogUtils.error("HTTP异常:", e);
        }
        LogUtils.info("[{}][{}] :[{}]->[{}]", evtNo, "KVM->EAP", trxId, returnMesg);
        return returnMesg;
    }

    public String getbodyHttpForClient(String evtNo, String url,String recipeId,String toolType, I inObj) {
        String trxId = inObj.getTrxId();
        String trxName = inObj.getTrxName();
        String actionFlag = inObj.getActionFlg();
        String requestMesg = JacksonUtils.object2String(inObj);
        String realUrl = url + "/" + toolType+ "/" + recipeId;
        LogUtils.info("[{}]",realUrl);
        LogUtils.info("[{}][{}]:[{}]->[{}]", evtNo,"EAP->Client", trxId, requestMesg);
        String returnMesg = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("traceId", evtNo);
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Content-Type", "application/json;charset=UTF-8");
            ResponseEntity<String> exchange = restTemplate.exchange(realUrl, HttpMethod.GET, new HttpEntity<>(requestMesg, headers), String.class);
            returnMesg = exchange.getBody();
        } catch (Exception e) {
            LogUtils.error("HTTP异常:", e);
        }
        LogUtils.info("[{}][{}] :[{}]->[{}]", evtNo, "Client->EAP", trxId, returnMesg);
        return returnMesg;
    }
    public String getlistHttpForClient(String evtNo, String url,String toolType, I inObj) {
        String trxId = inObj.getTrxId();
        String trxName = inObj.getTrxName();
        String actionFlag = inObj.getActionFlg();
        String requestMesg = JacksonUtils.object2String(inObj);
        String realUrl = url + "/" + toolType;
        LogUtils.info("[{}]",realUrl);
        LogUtils.info("[{}][{}]:[{}]->[{}]", evtNo,"EAP->Client", trxId, requestMesg);
        String returnMesg = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("traceId", evtNo);
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Content-Type", "application/json;charset=UTF-8");
            ResponseEntity<String> exchange = restTemplate.exchange(realUrl, HttpMethod.GET, new HttpEntity<>(requestMesg, headers), String.class);
            returnMesg = exchange.getBody();
        } catch (Exception e) {
            LogUtils.error("HTTP异常:", e);
        }
        LogUtils.info("[{}][{}] :[{}]->[{}]", evtNo, "Client->EAP", trxId, returnMesg);
        return returnMesg;
    }
}
