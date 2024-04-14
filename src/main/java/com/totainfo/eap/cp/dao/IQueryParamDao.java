package com.totainfo.eap.cp.dao;

import java.util.Map;

import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.QueryParamInfo;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 10:15
 */
public interface IQueryParamDao {
    void addQueryParamInfo(QueryParamInfo queryParamInfo);
    void delQueryParamInfo(String paramId);
    Map<String,QueryParamInfo> getAllQueryParamInfo();
    QueryParamInfo getQueryParamInfo(String paramId);

}
