package com.totainfo.eap.cp.job;

import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.commdef.GenericDataDef;
import com.totainfo.eap.cp.dao.IAlarmDao;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.AlarmInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.EmsHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.util.DateUtils;
import com.totainfo.eap.cp.util.GUIDGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;
import static com.totainfo.eap.cp.util.DateUtils.getDiffMill;

/**
 * @author xiaobin.Guo
 * @date 2024年01月15日 15:17
 */
@Component
public class AlarmClearJob {

    @Resource
    private ILotDao lotDao;

    @Resource
    private IAlarmDao alarmDao;

    @Scheduled(initialDelay = 1000, fixedDelay = 10*1000)
    public void job(){

        Map<String, AlarmInfo> alarmInfoMap = alarmDao.getAlarmInfo();
        if(alarmInfoMap == null || alarmInfoMap.isEmpty()){
            return;
        }

        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            lotInfo = new LotInfo();
            lotInfo.setLotId(_SPACE);
        }
        long diffTime;
        Timestamp timestamp;
        AlarmInfo pvAlarmInfo;
        String evtNo = GUIDGenerator.javaGUID();
        Timestamp crTimestamtp = DateUtils.getTimestamp();
        for(Map.Entry<String,AlarmInfo> entry:alarmInfoMap.entrySet()){
            pvAlarmInfo = entry.getValue();
            timestamp = Timestamp.valueOf(pvAlarmInfo.getTime());
            diffTime = DateUtils.getDiffMill(crTimestamtp, timestamp);
            if(diffTime < -10000 || diffTime > 10000){
                EmsHandler.alarmReportToEms(evtNo,pvAlarmInfo.getAlarmCode(),pvAlarmInfo.getAlarmText(),lotInfo.getLotId(),"0", pvAlarmInfo.getAlarmImg());
                MesHandler.alarmReport(evtNo, pvAlarmInfo.getAlarmCode(), pvAlarmInfo.getAlarmText(),DateUtils.timestampFormat(crTimestamtp), pvAlarmInfo.getId()) ;

                alarmDao.removeAlarm(entry.getKey());
            }
        }
        MesHandler.eqptStatReport(evtNo, GenergicStatDef.EqptStat.RUN,"无",lotInfo.getUserId());

    }
}
