package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IStateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Repository;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

@Repository("stateDao")
public class StateDao implements IStateDao {
    public static final String KEY = "EQPT:%s:STATEINFO";

    @Override
    public void addStateInfo(StateInfo stateInfo){
        String key = String.format(KEY, GenericDataDef.equipmentNo);
        String s = JacksonUtils.object2String(stateInfo);
        RedisHandler.set(key, s);
    }

    @Override
    public void setStateInfo(String step, String state, String lotId){
        StateInfo stateInfo  = new StateInfo();
        stateInfo.setStep(step);
        stateInfo.setState(state);
        stateInfo.setLotNo(lotId);
        String key = String.format(KEY, GenericDataDef.equipmentNo);
        String s = JacksonUtils.object2String(stateInfo);
        RedisHandler.set(key, s);
    }

    @Override
    public StateInfo getStateInfo(){
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        StateInfo stateInfo = JacksonUtils.string2Object(RedisHandler.get(key), StateInfo.class);
        return stateInfo;
    }

    @Override
    public void removeState(){
        String key = String.format(KEY,GenericDataDef.equipmentNo);
        RedisHandler.remove(key);
    }
}
