package com.denfop.integration.rs;

import com.denfop.api.Recipes;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.Input;
import com.denfop.api.recipe.RecipeOutput;
import com.raoulvdberge.refinedstorage.RSItems;
import ic2.api.recipe.IRecipeInputFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class RefinedStorageIntegration {

    public static void init() {
        addRecipe(new ItemStack(RSItems.PROCESSOR, 1, 0), new ItemStack(RSItems.PROCESSOR, 1, 3), 0.5F);
        addRecipe(new ItemStack(RSItems.PROCESSOR, 1, 1), new ItemStack(RSItems.PROCESSOR, 1, 4), 0.5F);
        addRecipe(new ItemStack(RSItems.PROCESSOR, 1, 2), new ItemStack(RSItems.PROCESSOR, 1, 5), 0.5F);

    }

    public static void addRecipe(ItemStack input, ItemStack output, float experience) {
        final IRecipeInputFactory inputFactory = ic2.api.recipe.Recipes.inputFactory;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("experience", experience);
        Recipes.recipes.addRecipe(
                "furnace",
                new BaseMachineRecipe(
                        new Input(
                                inputFactory.forStack(input)
                        ),
                        new RecipeOutput(nbt, output)
                )
        );
    }

}
