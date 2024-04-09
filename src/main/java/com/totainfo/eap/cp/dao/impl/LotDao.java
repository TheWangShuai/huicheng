package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 10:15
 */
@Repository("lotDao")
public class LotDao implements ILotDao {

    public static final String LOTINFO_KEY = "EQPT:%s:LOTINFO";
    public static final String LOTCLIENTINFO_KEY = "EQPT:%s:LOTCLIENTINFO";
    public static final String DIECOUNT_KEY = "EQPT:%s:DIECOUNT";
    public static final String WAFERTIME_KEY = "EQPT:%s:WAFERTIME";

    @Override
    public void addLotInfo(LotInfo lotInfo){
        String key = String.format(LOTINFO_KEY, equipmentNo);
        RedisHandler.set(key, lotInfo);
    }

    @Override
    public void addClientLotInfo(EAPReqLotInfoOB eapReqLotInfoOB) {
        String key = String.format(LOTCLIENTINFO_KEY, equipmentNo);
        RedisHandler.set(key, eapReqLotInfoOB);
    }

    @Override
    public EAPReqLotInfoOB getClientLotInfo() {
        String key = String.format(LOTCLIENTINFO_KEY, equipmentNo);
        return RedisHandler.get(key);
    }

    @Override
    public void removeClientLotInfo() {
        String key = String.format(LOTCLIENTINFO_KEY, equipmentNo);
        RedisHandler.remove(key);
    }

    @Override
    public void addWaferTime(Map<String, String> map) {
        String key = String.format(WAFERTIME_KEY, equipmentNo);
        RedisHandler.set(key, map);
    }

    @Override
    public Map<String, String> getWaferTime() {
        String key = String.format(WAFERTIME_KEY, equipmentNo);
        return RedisHandler.get(key);
    }

    @Override
    public LotInfo getCurLotInfo(){
        String key = String.format(LOTINFO_KEY, equipmentNo);
        return RedisHandler.get(key);
    }

    @Override
    public void addDieCount(DieCountInfo dieCountInfo) {
        String key = String.format(DIECOUNT_KEY, equipmentNo);
        RedisHandler.set(key, dieCountInfo);
    }

    @Override
    public DieCountInfo getDieCount() {
        String key = String.format(DIECOUNT_KEY, equipmentNo);
        return RedisHandler.get(key);
    }

    @Override
    public void removeLotInfo(){
        String key = String.format(LOTINFO_KEY, equipmentNo);
        RedisHandler.remove(key);
    }
}
