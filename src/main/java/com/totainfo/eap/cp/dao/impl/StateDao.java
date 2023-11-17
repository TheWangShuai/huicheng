package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Repository;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

@Repository("stateDao")
public class StateDao implements IStateDao {
    public static final String KEY = "EQPT:%s";

    @Override
    public void addStateInfo(StateInfo stateInfo){
        String key = String.format(KEY,"state");
        String s = JacksonUtils.object2String(stateInfo);
        RedisHandler.set(key, s);
    }

    @Override
    public StateInfo getStateInfo(){
        String key = String.format(KEY,"state");
        StateInfo stateInfo = JacksonUtils.string2Object(RedisHandler.get(key), StateInfo.class);
        return stateInfo;
    }
}
