package com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody;

import com.totainfo.eap.cp.base.trx.BaseTrxI;

/**
 * @author xiaobin.Guo
 * @date 2022年11月17日 13:25
 */
public class RmsQueryRecipeBodyI extends BaseTrxI {
     private String evtUsr;
     private String fabIdFk;
     private String toolId;
     private String recipeId;
     private String recipeLevel;

    public String getEvtUsr() {
        return evtUsr;
    }

    public void setEvtUsr(String evtUsr) {
        this.evtUsr = evtUsr;
    }

    public String getFabIdFk() {
        return fabIdFk;
    }

    public void setFabIdFk(String fabIdFk) {
        this.fabIdFk = fabIdFk;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeLevel() {
        return recipeLevel;
    }

    public void setRecipeLevel(String recipeLevel) {
        this.recipeLevel = recipeLevel;
    }
}
