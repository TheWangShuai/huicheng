package com.totainfo.eap.cp.entity;

import com.totainfo.eap.cp.mode.ValidationItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author WangShuai
 * @date 2024/4/13
 */
@Data
public class ValidationInfo {
    private static List<ValidationItem> validationItemList;
    public static List<ValidationItem> getValidationItemList(){
        return validationItemList;
    }
    public static void setValidationItemList(List<ValidationItem> validationItemList){
        ValidationInfo.validationItemList =validationItemList;
    }
    public static Map<String,Boolean> paramMap = new ConcurrentHashMap<>();
}