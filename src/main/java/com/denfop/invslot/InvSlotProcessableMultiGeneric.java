package com.denfop.invslot;

import com.denfop.api.inv.IInvSlotProcessableMulti;
import com.denfop.tiles.base.TileEntityMultiMachine;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InvSlotProcessableMultiGeneric extends InvSlot implements IInvSlotProcessableMulti {

    protected IMachineRecipeManager recipeManager;

    public InvSlotProcessableMultiGeneric(
            IInventorySlotHolder<?> base,
            String name,
            int count,
            IMachineRecipeManager recipeManager
    ) {
        super(base, name, Access.I, count);
        this.recipeManager = recipeManager;
    }

    public void put(int index, ItemStack content) {
        super.put(index, content);
        if (this.base instanceof TileEntityMultiMachine) {
           ((TileEntityMultiMachine) this.base).getOutput(index);

        }
    }

    @Override
    public ItemStack get1(final int i) {
        return this.get(i);
    }

    public boolean accepts(ItemStack stack) {
        if (stack.getItem() instanceof ItemUpgradeModule) {
            return false;
        } else {
            ItemStack tmp = stack.copy();
            return this.getOutputFor(tmp, true) != null;
        }
    }

    public ItemStack getItem() {

        int var2 = this.size();

        for (int var3 = 0; var3 < var2; ++var3) {
            ItemStack stack = this.get1(var3);
            if (!StackUtil.isEmpty(stack)) {
                return stack;
            }
        }

        return null;
    }

    public RecipeOutput process(int slotId) {
        ItemStack input = this.get(slotId);
        if (input.isEmpty()) {
            return null;
        } else {
            MachineRecipeResult output = this.getOutputFor(input, false);
            if (output == null) {
                return null;
            } else {
                if (output.getRecipe().getOutput() instanceof List) {
                    List<ItemStack> stack = (List<ItemStack>) output.getRecipe().getOutput();
                    return new RecipeOutput(output.getRecipe().getMetaData(), stack);
                } else {
                    return new RecipeOutput(output.getRecipe().getMetaData(), (ItemStack) output.getRecipe().getOutput());

                }
            }
        }
    }

    public void consume(int slotId) {
        ItemStack input = this.get(slotId);
        if (input.isEmpty()) {
            throw new IllegalStateException("consume from empty slot");
        } else {
            MachineRecipeResult output = this.getOutputFor(input, true);

            if (output == null) {
                throw new IllegalStateException("consume without a processing result");
            } else {
                this.put(slotId, (ItemStack) output.getAdjustedInput());


            }
        }
    }

    public void put1(int i, ItemStack stack) {
        this.put(i, stack);
    }

    protected MachineRecipeResult getOutputFor(ItemStack input, boolean adjustInput) {
        return this.recipeManager.apply(input, adjustInput);
    }

    public void setRecipeManager(IMachineRecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }
//


}
