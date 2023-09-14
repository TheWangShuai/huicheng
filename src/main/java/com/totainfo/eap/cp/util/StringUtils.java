package com.totainfo.eap.cp.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaobinGuo on 2017/10/26.
 */
public class StringUtils {

    public static String reverse(String srcStr){
        StringBuffer strBuf = new StringBuffer(srcStr);
        return strBuf.reverse().toString();
    }

    public static  boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }


    public static boolean isEmpty(String str){
       return str == null || "".equals(str);
    }

    public static boolean isNotEmpty(String str){
        return str != null && !"".equals(str);
    }

}
