package com.totainfo.eap.cp.dao;

import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.StateInfo;

import java.util.Map;

public interface IAlarmDao {
    void addAlarmInfo(AlarmInfo alarmInfo);

    Map<String, AlarmInfo> getAlarmInfo();

    void removeAlarm(String alarmCode);
}
