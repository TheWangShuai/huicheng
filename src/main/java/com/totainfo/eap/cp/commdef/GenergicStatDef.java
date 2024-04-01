package com.totainfo.eap.cp.commdef;

public class GenergicStatDef {

    public static class MdcKey{
       public static final String EVTNO = "EVTNO";
       public static final String EQPTID = "EQPTID";
    }


    public static class Constant{
        public static final String _SPACE ="";
        public static final String _YES = "Y";
        public static final String _NO = "N";
        public static final String _TRUE = "T";
        public static final String _FALSE = "F";
        public static final String SECS_VALIABLE = "i";
        public static final String SECS_SEPERATOR = "-";
        public static final String RETURN_CODE_OK = "0000000";
        public static final String SERVICE_EXCEPTION = "9999999";
        public static final String RETURN_MESG_OK = "SUCCESS";
    }

    public static class EventType{
        public static final String TRX_TYPE_REAL = "R";
        public static final String TRX_TYPE_INDEX = "I";
        public static final String TRX_TYPE_HANDSHAKE = "H";
    }

    public static class Protocel{
        public static final String HSMS = "HSMS";
        public static final String PLC = "PLC";
    }

    public static class LogType{
        public static final String BC_MESSAGE_LOG = "BC-LOGGER";
        public static final String SECS_LOG = "SECS-LOGGER";
        public static final String GPIB_LOG = "GPIB-LOGGER";
    }


    public static class StepName{
        public static final String FIRST = "1";
        public static final String SECOND = "2";
        public static final String THIRD = "3";
        public static final String FOURTH = "4";
        public static final String FITTH = "5";
        public static final String SIXTH = "6";
        public static final String SEVENTH = "7";
        public static final String EIGTH = "8";
        public static final String NIGHT = "9";
    }

    public static class StepStat{
        public static final String INPROCESS = "1";
        public static final String COMP = "2";
        public static final String FAIL = "3";
    }

    public static class MessageType{
        public static final int INFO = 1;
        public static final int ERROR = 2;
    }

    public static class KVMOperateState{
        public static final String Pass = "pass";
        public static final String Fail = "fail";
    }

    public static class EqptMode{
        public static final String Offline = "0";
        public static final String Online = "1";
    }

    public static class EqptStat{
        public static final String RUN = "RUN";
        public static final String DOWN = "DOWN";
        public static final String IDLE = "IDLE";
    }

    public static class RMSResult{
        public static final String TRUE = "TRUE";
        public static final String FALSE = "FALSE";
    }
}


