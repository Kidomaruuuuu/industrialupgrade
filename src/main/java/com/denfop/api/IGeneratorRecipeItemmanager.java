package com.denfop.api;

import com.denfop.api.recipe.RecipeOutput;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface IGeneratorRecipeItemmanager {

    void addRecipe(IRecipeInput var1, Integer var2, ItemStack... var3);

    RecipeOutput getOutputFor(ItemStack var1, boolean var2);

    Map<IRecipeInput, RecipeOutput> getRecipes();

}
