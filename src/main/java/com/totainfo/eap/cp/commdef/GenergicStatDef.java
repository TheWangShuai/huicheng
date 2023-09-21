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
    }

    public static class SecsProtocel{
        public static final String HSMS = "HSMS";
        public static final String SECS1 = "SECS1";
        public static final String RS232 = "RS232";
    }

    public static class SecsDateType{
        public static final String ASCII = "A";
        public static final String BINARY = "BINARY";
        public static final String U1 = "U1";
        public static final String U2 = "U2";
        public static final String U4 = "U4";
        public static final String U8 = "U8";
        public static final String I1 = "I1";
        public static final String I2 = "I2";
        public static final String I4 = "I4";
        public static final String I8 = "I8";
        public static final String BOOLEAN = "BOOLEAN";

    }

    public static class DriverType {
        public static final String EAP = "EAP"; //EAP
        public static final String EQP = "EQP"; //设备
    }

    public static class  TransferDirect{
        public static final String EQP_BC = "EQP->BC";
        public static final String EAP_BC = "EAP->BC";
        public static final String BC_EQP = "BC->EQP";
        public static final String BC_EAP = "BC -EAP";
    }


    public static class ControlMode{
        public static final String BCOffline = "1";
        public static final String AttemptOnline = "2";
        public static final String EAPOffline = "3";
        public static final String OnlineLocal = "4";
        public static final String OnlineRemote = "5";
    }

    public static class SecsKey{
        public static final String VID = "VID";
        public static final String CEID = "CEID";
        public static final String RPTID = "RPTID";
        public static final String DATAID = "DATAID";
        public static final String ECID = "ECID";

    }

    public static class AlarmAction{
        public static final String AlarmClear = "1";
        public static final String AlarmSet = "2";
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


