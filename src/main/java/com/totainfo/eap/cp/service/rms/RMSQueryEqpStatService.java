package com.totainfo.eap.cp.service.rms;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef.EqptMode;
import com.totainfo.eap.cp.dao.IEqptDao;
import com.totainfo.eap.cp.entity.EqptInfo;
import com.totainfo.eap.cp.trx.rms.RmsQueryEqpStat.RmsQueryEqpStatI;
import com.totainfo.eap.cp.trx.rms.RmsQueryEqpStat.RmsQueryEqpStatO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xiaobin.Guo
 * @date 2023年09月19日 15:23
 */
@Service("rmsQueryEqpState")
public class RMSQueryEqpStatService extends EapBaseService<RmsQueryEqpStatI, RmsQueryEqpStatO> {

    @Resource
    private IEqptDao eqptDao;

    @Override
    public void mainProc(String evtNo, RmsQueryEqpStatI inTrx, RmsQueryEqpStatO outTrx) {
        outTrx.setToolId(inTrx.getToolId());
        EqptInfo eqptInfo = eqptDao.getEqpt();
        if(EqptMode.Online.equals(eqptInfo.getEqptMode())){
            outTrx.setToolSat("ON");
        }else{
            outTrx.setToolSat("OFF");
        }
    }
}
