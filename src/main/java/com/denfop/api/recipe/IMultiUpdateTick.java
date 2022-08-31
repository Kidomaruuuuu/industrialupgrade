package com.denfop.api.recipe;

import com.denfop.componets.CoolComponent;
import com.denfop.componets.EXPComponent;
import com.denfop.tiles.mechanism.EnumTypeMachines;

public interface IMultiUpdateTick extends IUpdateTick {

    MachineRecipe getRecipeOutput(int slotId);

    void setRecipeOutput(MachineRecipe output, int slotId);


}
