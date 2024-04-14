package com.totainfo.eap.cp.mode;

import lombok.Data;

/**
 * @author WangShuai
 * @date 2024/4/12
 */
@Data
public class ValidationItem {
    private String paramNo;
    private String paramId;
    private String paramName;
    private String paramType;
    private String defaultValue;
    private String remark;
    private String actualValue;
}