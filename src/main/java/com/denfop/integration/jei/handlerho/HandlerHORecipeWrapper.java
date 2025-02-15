package com.denfop.integration.jei.handlerho;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandlerHORecipeWrapper implements IRecipeWrapper {

    private final ItemStack inputstack;
    private final List<ItemStack> outputstack;


    public HandlerHORecipeWrapper(HandlerHOHandler container) {


        this.inputstack = container.getInput();
        this.outputstack = container.getOutput();

    }

    public ItemStack getInput() {
        return inputstack;
    }

    public List<List<ItemStack>> getInputs() {
        ItemStack inputs = this.inputstack;
        List<ItemStack> stack = new ArrayList<>();
        if (OreDictionary.getOreIDs(inputs).length > 0) {
            int id = OreDictionary.getOreIDs(inputs)[0];
            stack.addAll(OreDictionary.getOres(OreDictionary.getOreName(id)));
        } else {
            stack.add(inputs);
        }
        return inputs.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    public List<ItemStack> getOutputs() {
        return this.outputstack;
    }


    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, this.getInputs());
        ingredients.setOutputs(ItemStack.class, this.getOutputs());
    }


    public List<ItemStack> getOutput() {
        return outputstack;
    }

    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

}
