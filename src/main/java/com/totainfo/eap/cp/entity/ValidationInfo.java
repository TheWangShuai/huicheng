package com.totainfo.eap.cp.entity;

import com.totainfo.eap.cp.mode.ValidationItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangShuai
 * @date 2024/4/13
 */
@Data
public class ValidationInfo {
    //初始值为true,进行第一次查询
    public static Boolean replyFlag = true;
    private static List<ValidationItem> validationItemList;
    public static List<ValidationItem> getValidationItemList(){
        return validationItemList;
    }
    public static void setValidationItemList(List<ValidationItem> validationItemList){
        ValidationInfo.validationItemList =validationItemList;
    }
}