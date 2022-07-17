package com.denfop.tiles.base;

import com.denfop.api.recipe.IUpdateTick;
import com.denfop.api.recipe.InvSlotRecipes;
import com.denfop.api.recipe.MachineRecipe;
import com.denfop.invslot.InvSlotUpgrade;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class WorldCollector extends TileEntityElectricMachine implements IUpdateTick {

    private final EnumTypeCollector enumTypeCollector;
    private final InvSlotRecipes inputSlot;
    private final InvSlotUpgrade upgradeSlot;
    private final int defaultEnergyConsume;
    private final int defaultOperationLength;
    private final int defaultEnergyStorage;
    private final int defaultTier;
    MachineRecipe machineRecipe;
    private int energyConsume;
    private int operationLength;
    private int operationsPerTick;

    public WorldCollector(EnumTypeCollector enumTypeCollector) {
        super(5000, 1, 1);
        this.enumTypeCollector = enumTypeCollector;
        this.inputSlot = new InvSlotRecipes(this, enumTypeCollector.name().toLowerCase() + "collector", this);
        this.machineRecipe = null;
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
        this.defaultEnergyConsume = this.energyConsume = 40;
        this.defaultOperationLength = this.operationLength = 800;
        this.operationsPerTick = 1;
        this.defaultTier = 1;
        this.defaultEnergyStorage = 5000;
    }

    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            inputSlot.load();
            this.setOverclockRates();

        }

    }

    public void setOverclockRates() {
        this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
        this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
        this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
        int tier = this.upgradeSlot.getTier(this.defaultTier);
        this.energy.setSinkTier(tier);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(
                this.defaultEnergyStorage,
                this.defaultOperationLength,
                this.defaultEnergyConsume
        ));


    }

    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public GuiScreen getGui(final EntityPlayer entityPlayer, final boolean b) {
        return null;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public MachineRecipe getRecipeOutput() {
        return this.machineRecipe;
    }

    @Override
    public void setRecipeOutput(final MachineRecipe output) {
        this.machineRecipe = output;
    }

}
