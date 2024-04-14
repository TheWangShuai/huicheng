package com.totainfo.eap.cp.trx.client.EAPValidation;

import com.totainfo.eap.cp.base.trx.BaseTrxI;
import lombok.Data;

import java.util.List;

/**
 * @author WangShuai
 * @date 2024/4/13
 */
@Data
public class EAPValidationI extends BaseTrxI {
    private List<EAPValidationIA> infos;
}