package com.totainfo.eap.cp.dao;

import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.StateInfo;

public interface IAlarmDao {
    void addAlarmInfo(AlarmInfo alarmInfo);

    AlarmInfo getAlarmInfo();
}
