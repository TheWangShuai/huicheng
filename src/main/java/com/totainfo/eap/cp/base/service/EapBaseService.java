package com.totainfo.eap.cp.base.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.totainfo.eap.cp.base.trx.BaseTrxI;
import com.totainfo.eap.cp.base.trx.BaseTrxO;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;

import java.lang.reflect.ParameterizedType;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_MESG_OK;
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.SERVICE_EXCEPTION;


/**
 * @author xiaobin.Guo
 * @date 2022年11月07日 16:23
 */
public abstract class EapBaseService<I extends BaseTrxI, O extends BaseTrxO>  implements IEapBaseInterface {
    @Override
    public String subMainProc(String evtNo, String message){

        LogUtils.info("[{}][{}][InTrx:{}]", evtNo, this.getClass().getSimpleName(), message);
        ParameterizedType t = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class<I> inClass = (Class<I>) t.getActualTypeArguments()[0];
        Class<O> outClass = (Class<O>) t.getActualTypeArguments()[1];
        String s = message.toString();
        I inTrx = null;
        O ouTrx = null;
        try{
            ouTrx = outClass.newInstance();
            ouTrx.setRtnCode(RETURN_CODE_OK);
            ouTrx.setRtnMesg(RETURN_MESG_OK);
            inTrx = JacksonUtils.string2Object(message, inClass);
            mainProc(evtNo, inTrx, ouTrx);
        }catch (Exception e){
            ouTrx.setRtnCode(SERVICE_EXCEPTION);
            ouTrx.setRtnMesg("系统发生异常，请联系管理员");
            LogUtils.error("[" + this.getClass().getName()+"][" + evtNo + "]异常", e);
        }
        String returnMsg = JacksonUtils.object2String(ouTrx);
        LogUtils.info("[{}][{}][OutTrx:{}]", evtNo, this.getClass().getSimpleName(), returnMsg);
        return returnMsg;
    }

    public abstract void mainProc(String evtNo, I inTrx, O outTrx);
}
