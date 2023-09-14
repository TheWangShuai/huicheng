package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpHandler<I extends BaseTrxI> {


    @Autowired
    private RestTemplate restTemplate;

    @Value("${eqpt_url}")
    private String eqptUrl;

    public String postHttpForEqpt(String evt_no, I inObj) {
        String inTrxName =  inObj.getTrxId();
        String message = JacksonUtils.object2String(inObj);
        LogUtils.info("[{}][TOEQPT][{}][inTrx:[{}]]", evt_no, inTrxName, message);
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>(1);
        paramMap.add("strInMsg=", message);
        String returnMesg = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("tranceId", evt_no);
            MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(mediaType);
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramMap, headers);
            ResponseEntity<String> exchange = restTemplate.exchange(eqptUrl, HttpMethod.POST, httpEntity, String.class, paramMap);
            returnMesg = exchange.getBody();
        }catch (Exception e){
            LogUtils.error("HTTP异常", e);
        }finally {
            paramMap = null;
            message = null;
        }
        LogUtils.info("[{}][FREQPT][{}][inTrx:[{}]]", evt_no, inTrxName, returnMesg);
        return returnMesg;
    }
}
