package com.denfop.api.recipe;

import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;

public interface IRecipeInputStack {

    IRecipeInput getItemStack();

    boolean matched(ItemStack stack);
}
