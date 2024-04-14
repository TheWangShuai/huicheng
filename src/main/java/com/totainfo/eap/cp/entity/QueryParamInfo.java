package com.totainfo.eap.cp.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author WangShuai
 * @date 2024/4/14
 */
@Data
public class QueryParamInfo implements Serializable {
    private String paramId;
    private String paramValue;
    private String paramType;
}