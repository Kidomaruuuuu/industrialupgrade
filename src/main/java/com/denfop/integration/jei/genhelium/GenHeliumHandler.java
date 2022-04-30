package com.denfop.integration.jei.genhelium;


import com.denfop.api.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenHeliumHandler {

    private static final List<GenHeliumHandler> recipes = new ArrayList<>();
    private final FluidStack input2;
    private final int input;


    public GenHeliumHandler(
            int input, FluidStack input2
    ) {
        this.input = input;
        this.input2 = input2;
    }

    public static List<GenHeliumHandler> getRecipes() { // Получатель всех рецептов.
        if (recipes.isEmpty()) {
            initRecipes();
        }
        return recipes;
    }

    public static GenHeliumHandler addRecipe(
            int input, FluidStack input2
    ) {
        GenHeliumHandler recipe = new GenHeliumHandler(input, input2);
        if (recipes.contains(recipe)) {
            return null;
        }
        recipes.add(recipe);
        return recipe;
    }

    public static GenHeliumHandler getRecipe(ItemStack is) {
        if (is == null || is.isEmpty()) {
            return null;
        }
        for (GenHeliumHandler recipe : recipes) {
            return recipe;
        }
        return null;
    }

    public static void initRecipes() {
        for (Map.Entry<NBTTagCompound, FluidStack> container :
                Recipes.heliumgenerator.getRecipes().entrySet()) {
            addRecipe(container.getKey().getInteger("amount"), container.getValue());

        }
    }


    public int getEnergy() { // Получатель входного предмета рецепта.
        return input;
    }

    public FluidStack getOutput() { // Получатель входного предмета рецепта.
        return input2;
    }

}
