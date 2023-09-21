package com.totainfo.eap.cp.dao.impl;


import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptStat;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.handler.RedisHandler;
import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;


@Repository("eqptDao")
public class EqptDao implements IEqptDao {


    private String EQPT_INFO_KEY = "EQPTINFO:EQ:%s:KEY";
    private String EQPT_INFO_LOCK = "EQPTINFO:EQ:%s:LOCK";



    @Override
    public boolean addEqpt(EqptInfo eqptInfo) {
        String key = String.format(EQPT_INFO_KEY, equipmentNo);
        RedisHandler.set(key, eqptInfo);
        String lockKey = String.format(EQPT_INFO_LOCK, equipmentNo);
        RedisHandler.remove(lockKey);
        return true;
    }

    @Override
    public EqptInfo getEqpt() {
        String key = String.format(EQPT_INFO_KEY, equipmentNo);
        EqptInfo eqptInfo =  RedisHandler.get(key);
        if(eqptInfo == null){
            eqptInfo = new EqptInfo();
            eqptInfo.setEqptId(equipmentNo);
            eqptInfo.setEqptMode(EqptMode.Offline);
            eqptInfo.setEqptStat(EqptStat.IDLE);
        }
        return eqptInfo;
    }

    @Override
    public EqptInfo getEqptWithLock(){
         String key = String.format(EQPT_INFO_KEY, equipmentNo);
         String lockKey = String.format(EQPT_INFO_LOCK,equipmentNo);
         boolean lockFlg = false;
         while (!lockFlg){
             lockFlg = RedisHandler.setnx(lockKey, true, 5);   //加锁，设备信息和设备状态事件都会修改
             if(lockFlg){
                 break;
             }
             try {
                 TimeUnit.MILLISECONDS.sleep(10);
             } catch (InterruptedException e) {
                 LogUtils.error("延时异常", e);
             }
         }
         return RedisHandler.get(key);
    }
}
