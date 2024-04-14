package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.IQueryParamDao;
import com.totainfo.eap.cp.entity.QueryParamInfo;
import org.springframework.stereotype.Repository;

import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.handler.RedisHandler;

import java.util.Map;

@Repository("queryDao")
public class QueryParamDao implements IQueryParamDao {
    public static final String KEY = "EQPT:%s:QUERYPARAMINFO";
    @Override
    public void addQueryParamInfo(QueryParamInfo queryParamInfo) {
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        RedisHandler.hset(key,queryParamInfo.getParamId(),queryParamInfo);
    }

    @Override
    public void delQueryParamInfo(String paramId) {
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        RedisHandler.hdel(key,paramId);
    }

    @Override
    public Map<String, QueryParamInfo> getAllQueryParamInfo() {
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        return RedisHandler.hmget(key);
    }

    @Override
    public QueryParamInfo getQueryParamInfo(String paramId) {
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        return RedisHandler.hget(key,paramId);
    }


}
