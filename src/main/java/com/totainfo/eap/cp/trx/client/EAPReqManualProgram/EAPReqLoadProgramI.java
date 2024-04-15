package com.totainfo.eap.cp.trx.client.EAPReqManualProgram;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.naming.ldap.PagedResultsControl;

/**
 * @author WangShuai
 * @date 2024/4/11
 */
@Getter
@Setter
public class EAPReqLoadProgramI extends BaseTrxI {
    private String lotNo;
    private String userId;
}