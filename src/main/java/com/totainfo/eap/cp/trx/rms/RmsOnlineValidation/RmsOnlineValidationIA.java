package com.totainfo.eap.cp.trx.rms.RmsOnlineValidation;

/**
 * @author xiaobin.Guo
 * @date 2022年11月18日 11:19
 */
public class RmsOnlineValidationIA {
    private String lotId;
    private String recipeId;
    private String toolId;
    private String areaId;

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }
}
