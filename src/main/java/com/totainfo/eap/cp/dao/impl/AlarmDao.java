package com.totainfo.eap.cp.dao.impl;

import com.totainfo.eap.cp.dao.IAlarmDao;
import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.JacksonUtils;
import org.springframework.stereotype.Repository;

@Repository("alarmDao")
public class AlarmDao implements IAlarmDao {
    public static final String KEY = "EQPT:%s";
    @Override
    public void addAlarmInfo(AlarmInfo alarmInfo) {
        String key = String.format(KEY,"alarm");
        String s = JacksonUtils.object2String(alarmInfo);
        RedisHandler.set(key, s);

    }

    @Override
    public AlarmInfo getAlarmInfo() {
        String key = String.format(KEY,"alarm");
        AlarmInfo alarmInfo = JacksonUtils.string2Object(RedisHandler.get(key), AlarmInfo.class);
        return alarmInfo;
    }
}
