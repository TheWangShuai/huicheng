package com.totainfo.eap.cp.trx.client.EAPSyncEqpInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xiaobin.Guo
 * @date 2023年09月21日 10:56
 */

@Setter
@Getter
public class EAPSyncEqpInfoI extends BaseTrxI {
    private String userId;
    private String state;
    private String model;
    private String lotNo;
    private String probeCardId;
    private String foupLotNo;
    private String gpibState;
}
