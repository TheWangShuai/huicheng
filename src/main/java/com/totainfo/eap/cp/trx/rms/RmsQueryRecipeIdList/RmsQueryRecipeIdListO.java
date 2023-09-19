package com.totainfo.eap.cp.trx.rms.RmsQueryRecipeIdList;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2022年11月17日 13:22
 */
public class RmsQueryRecipeIdListO extends BaseTrxO {

    private List<RmsQueryRecipeIdListOA> bisRecipeVOList;

    public List<RmsQueryRecipeIdListOA> getBisRecipeVOList() {
        return bisRecipeVOList;
    }

    public void setBisRecipeVOList(List<RmsQueryRecipeIdListOA> bisRecipeVOList) {
        this.bisRecipeVOList = bisRecipeVOList;
    }
}
