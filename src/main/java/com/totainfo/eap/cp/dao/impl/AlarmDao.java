package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.IAlarmDao;
import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

@Repository("alarmDao")
public class AlarmDao implements IAlarmDao {

    private String EQPT_INFO_KEY = "EQPTALARM:EQ:%s:KEY";
    @Override
    public void addAlarmInfo(AlarmInfo alarmInfo) {
        String key = String.format(EQPT_INFO_KEY, equipmentNo);
        RedisHandler.hset(key, alarmInfo.getAlarmCode(), alarmInfo);

    }

    @Override
    public Map<String, AlarmInfo> getAlarmInfo() {
        String key = String.format(EQPT_INFO_KEY, equipmentNo);
        Map<String, AlarmInfo> alarmInfoMap = RedisHandler.hmget(key);
        if(alarmInfoMap == null){
            alarmInfoMap = new HashMap<>(0);
        }
        return alarmInfoMap;
    }

    @Override
    public void removeAlarm(String alarmCode){
        String key = String.format(EQPT_INFO_KEY, equipmentNo);
        RedisHandler.hdel(key, alarmCode);
    }
}
