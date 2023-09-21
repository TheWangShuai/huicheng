package com.totainfo.eap.cp.aop;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;


@Aspect
@Component
public class RmsAop {


    /**
     * 定义切入点，切入点为com.example.aop下的所有函数
     */
    @Pointcut("execution(* com.totainfo.eap.cp.service.rms.*.subMainProc(..))")
    public void webLog(){}


    @AfterReturning(returning = "ret",pointcut = "webLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Throwable {
        if (ret == null) {
            return;
        }
        Object[] args = joinPoint.getArgs();
        String evtNo = args[0].toString();
        ObjectNode objectNode = JacksonUtils.getJson2(ret.toString());
        String rtnCode = objectNode.get("rtnCode").textValue();
        String rtnMesg = objectNode.get("rtnMesg").textValue();
        if(!RETURN_CODE_OK.equals(rtnCode)){
            ClientHandler.sendMessage(evtNo, true, 1, rtnMesg);
        }
    }
}
