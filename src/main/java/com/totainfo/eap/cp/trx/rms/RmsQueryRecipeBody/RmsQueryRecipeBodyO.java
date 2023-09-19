package com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody;

import com.totainfo.eap.cp.base.trx.BaseTrxO;

/**
 * @author xiaobin.Guo
 * @date 2022年11月17日 13:28
 */
public class RmsQueryRecipeBodyO extends BaseTrxO {
    private String toolId;
    private String recipeBody;

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getRecipeBody() {
        return recipeBody;
    }

    public void setRecipeBody(String recipeBody) {
        this.recipeBody = recipeBody;
    }
}
