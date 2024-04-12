package com.totainfo.eap.cp.trx.ems.EAPRepRunWorkInfo;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import lombok.Getter;
import lombok.Setter;

/**
 * @author WangShuai
 * @date 2024/4/9
 */
@Setter
@Getter
public class EAPRepRunWorkInfoI extends BaseTrxI {

    private String title;
    private String equipmentNo;
    private String method_name;
    private String returnState;
    private String message;
    private String alarmImg;
    private String lotNo;

}