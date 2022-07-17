package com.denfop.api.recipe;

import com.denfop.api.Recipes;
import com.denfop.tiles.base.TileEntityConverterSolidMatter;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class InvSlotRecipes extends InvSlot {

    private final IBaseRecipe recipe;
    private final IUpdateTick tile;
    private final List<IRecipeInputStack> accepts;
    private List<BaseMachineRecipe> recipe_list;
    private FluidTank tank;

    public InvSlotRecipes(final TileEntityInventory base, IBaseRecipe baseRecipe, IUpdateTick tile) {
        super(base, "input", Access.I, baseRecipe.getSize());
        this.recipe = baseRecipe;
        this.recipe_list = Recipes.recipes.getRecipeList(this.recipe.getName());
        this.accepts = Recipes.recipes.getMap_recipe_managers_itemStack(this.recipe.getName());
        this.tile = tile;
        this.tank = null;
    }

    public InvSlotRecipes(final TileEntityInventory base, String baseRecipe, IUpdateTick tile) {
        this(base, Recipes.recipes.getRecipe(baseRecipe), tile);

    }

    public InvSlotRecipes(final TileEntityInventory base, String baseRecipe, IUpdateTick tile, FluidTank tank) {
        this(base, Recipes.recipes.getRecipe(baseRecipe), tile);
        this.tank = tank;
    }

    public void load() {
        this.recipe_list = Recipes.recipes.getRecipeList(this.recipe.getName());
    }

    @Override
    public void put(final int index, final ItemStack content) {
        super.put(index, content);
        final MachineRecipe recipe1 = this.process();
        this.tile.setRecipeOutput(recipe1);
        this.tile.onUpdate();
    }

    @Override
    public boolean accepts(final ItemStack itemStack) {
        return !itemStack.isEmpty() && !(itemStack.getItem() instanceof IUpgradeItem) && (recipe
                .getName()
                .equals("painter") || recipe
                .getName()
                .equals("upgradeblock") || recipe
                .getName()
                .equals("recycler") || accepts.contains(
                new RecipeInputStack(itemStack)));
    }

    public void consume(int number, int amount) {
        this.consume(number, amount, false, false);
    }

    public boolean continue_process(MachineRecipe recipe) {
        if (this.tank == null) {
            return Recipes.recipes.needContinue(recipe, this);
        } else {
            return Recipes.recipes.needContinue(recipe, this, tank);
        }
    }

    public void consume(int number, int amount, boolean simulate, boolean consumeContainers) {


        ItemStack stack = this.get(number);
        if (!stack.isEmpty() && stack.getCount() >= 1) {
            int currentAmount = Math.min(amount, stack.getCount());
            if (!simulate) {
                if (stack.getCount() == currentAmount) {
                    if (!consumeContainers && stack.getItem().hasContainerItem(stack)) {
                        this.put(number, stack.getItem().getContainerItem(stack));
                    } else {
                        this.put(number, ItemStack.EMPTY);
                    }
                } else {
                    stack.setCount(stack.getCount() - currentAmount);
                }
            }


        }


    }

    @Override
    public ItemStack get(final int index) {
        return super.get(index);
    }

    public MachineRecipe process() {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).isEmpty()) {
                return null;
            }
        }
        MachineRecipe output;
        output = this.getOutputFor();
        if (this.tile instanceof TileEntityConverterSolidMatter) {
            TileEntityConverterSolidMatter mechanism = (TileEntityConverterSolidMatter) this.tile;
            mechanism.getrequiredmatter(output.getRecipe().getOutput());
        }

        return output;
    }

    public BaseMachineRecipe consume() {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).isEmpty()) {
                return null;
            }
        }
        List<ItemStack> list = new ArrayList<>();
        this.forEach(list::add);
        if (this.tank == null) {
            return Recipes.recipes.getRecipeConsume(this.recipe, this.tile.getRecipeOutput(), this.recipe.consume(), list);
        } else {
            return Recipes.recipes.getRecipeOutputFluid(this.recipe, this.tile.getRecipeOutput(), this.recipe.consume(), list,
                    this.tank
            );

        }

    }

    public boolean continue_proccess(InvSlotOutput slot) {
        if (tile.getRecipeOutput() == null) {
            return false;
        }
        if (this.tank == null) {
            return

                    slot.canAdd(tile.getRecipeOutput().getRecipe().output.items) && this.get().getCount() >= tile
                            .getRecipeOutput().getList().get(0);
        } else {
            return slot.canAdd(tile.getRecipeOutput().getRecipe().output.items) && this.get().getCount() >= tile
                    .getRecipeOutput().getList().get(0) && this.tank.getFluidAmount() >= this.tile
                    .getRecipeOutput()
                    .getRecipe().input.getFluid().amount;
        }

    }

    private MachineRecipe getOutputFor() {
        List<ItemStack> list = new ArrayList<>();
        this.forEach(list::add);
        if (this.tank == null) {
            return Recipes.recipes.getRecipeMachineRecipeOutput(this.recipe, this.recipe_list, false, list);
        } else {
            return Recipes.recipes.getRecipeOutputMachineFluid(this.recipe, this.recipe_list, false, list, this.tank);
        }
    }

}
