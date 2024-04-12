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
import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.SERVICE_EXCEPTION;


@Aspect
@Component
public class LogAcpect {



    /**
     * 定义切入点，切入点为com.example.aop下的所有函数
     */
    @Pointcut("execution(* com.totainfo.eap.cp.base.service.EapBaseService.subMainProc(..))")
    public void eapLog(){}

    /**
     * 前置通知：在连接点之前执行的通知
     * @param joinPoint
     * @throws Throwable
     */

    @AfterReturning(returning = "ret",pointcut = "eapLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Throwable {
        if (ret == null) {
            return;
        }
        Object[] objects = joinPoint.getArgs();
        String outTrxMsg = ret.toString();

        ObjectNode objectNode = JacksonUtils.getJson2(outTrxMsg);
        String rtnCode = objectNode.get("rtnCode").textValue();
        String rtnMesg = objectNode.get("rtnMesg").textValue();
        if(!RETURN_CODE_OK.equals(rtnCode) && !SERVICE_EXCEPTION.equals(rtnCode)){
            ClientHandler.sendMessage(objects[0].toString(),false,1,rtnMesg);
        }
    }
}
