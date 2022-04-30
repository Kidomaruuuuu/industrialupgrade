package com.denfop.api.recipe;

import com.denfop.tiles.mechanism.EnumTypeMachines;

public interface IMultiUpdateTick extends IUpdateTick {

    BaseMachineRecipe getRecipeOutput(int slotId);

    void setRecipeOutput(BaseMachineRecipe output, int slotId);

    EnumTypeMachines getType();

}
