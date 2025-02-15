package com.denfop.integration.jei.plasticcreator;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlasticCrreatorWrapper implements IRecipeWrapper {


    private final ItemStack inputstack;
    private final ItemStack outputstack;
    private final ItemStack inputstack1;
    private final FluidStack inputstack2;

    public PlasticCrreatorWrapper(PlasticCrreatorHandler container) {


        this.inputstack = container.getInput();
        this.inputstack1 = container.getInput1();
        this.inputstack2 = container.getInput2();
        this.outputstack = container.getOutput();

    }

    public ItemStack getInput() {
        return inputstack;
    }

    public ItemStack getInput1() {
        return inputstack1;
    }

    public FluidStack getInput2() {
        return inputstack2;
    }

    public List<List<ItemStack>> getInputs() {
        ItemStack inputs = this.inputstack;
        List<ItemStack> stack = new ArrayList<>();
        stack.add(inputs);
        stack.add(this.inputstack1);
        return inputs.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    public List<ItemStack> getOutputs() {
        return new ArrayList(Collections.singleton(this.outputstack));
    }


    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, this.getInputs());
        ingredients.setInput(FluidStack.class, this.inputstack2);
        ingredients.setOutputs(ItemStack.class, this.getOutputs());
    }


    public ItemStack getOutput() {
        return outputstack;
    }

    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

}
