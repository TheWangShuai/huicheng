package com.totainfo.eap.cp.trx.rms.RmsQueryRecipeBody;

/**
 * @author xiaobin.Guo
 * @date 2022年11月24日 9:59
 */
public class RmsQueryRecipeBodyOA {
    private String recipeId;
    private String toolId;
    private String recipeBody;

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

    public String getRecipeBody() {
        return recipeBody;
    }

    public void setRecipeBody(String recipeBody) {
        this.recipeBody = recipeBody;
    }
}
