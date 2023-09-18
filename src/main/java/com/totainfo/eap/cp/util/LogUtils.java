package com.totainfo.eap.cp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totainfo.eap.cp.commdef.GenergicStatDef.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class LogUtils {


    private static ObjectMapper objectMapper = new ObjectMapper();

    private  static Logger bcLogger = LoggerFactory.getLogger(LogType.BC_MESSAGE_LOG);
    private  static Logger secsLogger = LoggerFactory.getLogger(LogType.SECS_LOG);
    public static Logger logger = LoggerFactory.getLogger(LogUtils.class);



    public static void info(String msg, Object... args){
        logger.info(msg, args);
    }

    public static void error(String msg, Object... args){
        logger.error(msg, args);
    }

    public static void error(String msg, Throwable throwable) {
        logger.error(msg, throwable);
    }

    public static void warn(String msg, Object... args){
        logger.warn(msg, args);
    }

    private static void info(Logger logger, String msg, Object... args) {
        checkNull(logger);
        logger.info(msg, args);
    }

    private static void info(Logger logger, String msg, Throwable throwable) {
        checkNull(logger);
        logger.info(msg, throwable);
    }

    private static void warn(Logger logger, String msg, Object... args) {
        checkNull(logger);
        logger.warn(msg, args);
    }

    private static void warn(Logger logger, String msg, Throwable throwable) {
        checkNull(logger);
        logger.warn(msg, throwable);
    }

    private static void error(Logger logger, String msg, Object... args) {
        checkNull(logger);
        logger.error(msg, args);
    }

    private static void error(Logger logger, String msg, Throwable throwable) {
        checkNull(logger);
        logger.error(msg, throwable);
    }

    private static void checkNull(Logger logger) {
        Optional.ofNullable(logger).orElseThrow(NullPointerException::new);
    }



}
