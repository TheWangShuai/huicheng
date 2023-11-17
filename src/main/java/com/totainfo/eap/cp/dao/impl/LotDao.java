package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.stereotype.Repository;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 10:15
 */
@Repository("lotDao")
public class LotDao implements ILotDao {

    public static final String LOTINFO_KEY = "EQPT:%s:LOTINFO";

    @Override
    public void addLotInfo(LotInfo lotInfo){
        String key = String.format(LOTINFO_KEY, equipmentNo);
        RedisHandler.set(key, lotInfo);
    }

    @Override
    public LotInfo getCurLotInfo(){
        String key = String.format(LOTINFO_KEY, equipmentNo);
        return RedisHandler.get(key);
    }
}
