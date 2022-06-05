package com.denfop.api.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Objects;

public class RecipeInputStack implements IRecipeInputStack{

    private final IRecipeInput input;

    public RecipeInputStack(IRecipeInput input){
        this.input = input;

    }
    public RecipeInputStack(ItemStack input){
        this.input = Recipes.inputFactory.forStack(input);

    }
    @Override
    public IRecipeInput getItemStack() {
        return input;
    }

    @Override
    public boolean matched(final ItemStack stack) {
        for(ItemStack input : getItemStack().getInputs())
            if(input.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack.getItemDamage() == stack.getItemDamage()))
                return true;
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeInputStack that = (RecipeInputStack) o;
        for(ItemStack input : getItemStack().getInputs())
            for(ItemStack input1 : that.getItemStack().getInputs()) {
                if(input.getItem() == input1.getItem() && (input1.getItemDamage() == OreDictionary.WILDCARD_VALUE || input1.getItemDamage() == input1.getItemDamage()))
                    return true;
            }
        return false;
    }



}
