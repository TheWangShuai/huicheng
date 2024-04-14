package com.totainfo.eap.cp.trx.ems.EMSStatusReport;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EMSStatusReportI extends BaseTrxI {
    private String eqpCommStatus;

    private String lastState;

    private String equipmentNo;

    private String remark;

    private String lastStateVal;

    private String lotNo;
}
