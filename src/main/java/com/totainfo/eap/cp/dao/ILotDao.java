package com.totainfo.eap.cp.dao;

import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;

import javax.print.DocFlavor;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobin.Guo
 * @date 2023年09月18日 10:15
 */
public interface ILotDao {
    void addLotInfo(LotInfo lotInfo);

    LotInfo getCurLotInfo();
    void addDieCount(DieCountInfo dieCountInfo);
    DieCountInfo getDieCount();
    void removeLotInfo();

    void addClientLotInfo(EAPReqLotInfoOB eapReqLotInfoOB);

    EAPReqLotInfoOB getClientLotInfo();

    void removeClientLotInfo();
    void addWaferTime(Map<String, String> map);

    Map<String,String> getWaferTime();
}
