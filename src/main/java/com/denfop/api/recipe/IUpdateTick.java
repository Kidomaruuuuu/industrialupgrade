package com.denfop.api.recipe;

public interface IUpdateTick {

    void onUpdate();

    BaseMachineRecipe getRecipeOutput();

    void setRecipeOutput(BaseMachineRecipe output);

}
