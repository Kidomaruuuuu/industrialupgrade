package com.denfop.tiles.base;

import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.IUpdateTick;
import com.denfop.api.recipe.InvSlotRecipes;
import com.denfop.container.ContainerTripleElectricMachine;
import com.denfop.tiles.mechanism.TileEntityAdvAlloySmelter;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class TileEntityTripleElectricMachine extends TileEntityStandartMachine
        implements IHasGui, INetworkTileEntityEventListener, IUpgradableBlock, IUpdateTick {

    public final InvSlotDischarge dischargeSlot;
    public final int defaultEnergyConsume;
    public final int defaultOperationLength;
    public final int defaultTier;
    public final int defaultEnergyStorage;
    public final InvSlotRecipes inputSlotA;
    public final InvSlotUpgrade upgradeSlot;
    protected final String name;
    protected final EnumTripleElectricMachine type;
    public int energyConsume;
    public int operationLength;
    public int operationsPerTick;
    public AudioSource audioSource;
    public short temperature;
    public BaseMachineRecipe output;
    protected short progress;
    protected double guiProgress;

    public TileEntityTripleElectricMachine(
            int energyPerTick,
            int length,
            int outputSlots,
            String name,
            EnumTripleElectricMachine type
    ) {
        this(energyPerTick, length, outputSlots, 1, name, type);
    }

    public TileEntityTripleElectricMachine(
            int energyPerTick, int length, int outputSlots, int aDefaultTier, String name,
            EnumTripleElectricMachine type
    ) {
        super(outputSlots);
        this.progress = 0;
        this.defaultEnergyConsume = this.energyConsume = energyPerTick;
        this.defaultOperationLength = this.operationLength = length;
        this.defaultTier = aDefaultTier;
        this.defaultEnergyStorage = energyPerTick * length;
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
        this.name = name;
        this.inputSlotA = new InvSlotRecipes(this, type.recipe_name, this);
        this.type = type;
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, aDefaultTier, false, InvSlot.InvSide.ANY);

        this.energy = this.addComponent(Energy
                .asBasicSink(this, (double) energyPerTick * length, aDefaultTier)
                .addManagedSlot(this.dischargeSlot));
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public BaseMachineRecipe getRecipeOutput() {
        return this.output;
    }

    @Override
    public void setRecipeOutput(final BaseMachineRecipe output) {
        this.output = output;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.progress = nbttagcompound.getShort("progress");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setShort("progress", this.progress);
        return nbttagcompound;
    }

    protected void initiate(int soundEvent) {
        IC2.network.get(true).initiateTileEntityEvent(this, soundEvent, true);
    }

    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
            inputSlotA.load();
            this.getOutput();
        }

    }

    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }

    }

    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
        }

    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;


        if (this.output != null && this.outputSlot.canAdd(output.output.items) && this.energy.getEnergy() >= this.energyConsume) {
            if (this.type.equals(EnumTripleElectricMachine.ADV_ALLOY_SMELTER)) {
                if (this.output.output.metadata.getShort("temperature") == 0 || output.output.metadata.getInteger("temperature") > ((TileEntityAdvAlloySmelter) this).temperature) {
                    return;
                }
            }
            setActive(true);
            if (this.progress == 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
            }
            this.progress = (short) (this.progress + 1);
            this.energy.useEnergy(this.energyConsume);
            double k = this.progress;

            this.guiProgress = (k / this.operationLength);
            if (this.progress >= this.operationLength) {
                this.guiProgress = 0;
                operate(output);
                needsInvUpdate = true;
                this.progress = 0;
                IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
            }
        } else {
            if (this.type.equals(EnumTripleElectricMachine.ADV_ALLOY_SMELTER)) {
                if (((TileEntityAdvAlloySmelter) this).temperature > 0) {
                    ((TileEntityAdvAlloySmelter) this).temperature--;
                }
            }
            if (this.progress != 0 && getActive()) {
                IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
            }
            if (output == null) {
                this.progress = 0;
            }
            setActive(false);
        }
        for (int i = 0; i < this.upgradeSlot.size(); i++) {
            ItemStack stack = this.upgradeSlot.get(i);
            if (stack != null && stack.getItem() instanceof IUpgradeItem) {
                if (((IUpgradeItem) stack.getItem()).onTick(stack, this)) {
                    needsInvUpdate = true;
                }
            }
        }

        if (needsInvUpdate) {
            super.markDirty();
        }

    }

    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
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
        dischargeSlot.setTier(tier);
    }

    public void operate(BaseMachineRecipe output) {
        for (int i = 0; i < this.operationsPerTick; i++) {
            List<ItemStack> processResult = output.output.items;
            for (int j = 0; j < this.upgradeSlot.size(); j++) {
                ItemStack stack = this.upgradeSlot.get(j);
                if (stack != null && stack.getItem() instanceof IUpgradeItem) {
                    ((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, processResult);
                }
            }
            operateOnce(output, processResult);
            for (int k = 0; k < this.inputSlotA.size(); k++) {
                if (this.inputSlotA.get(k).isEmpty()) {
                    output = getOutput();
                    break;
                }
            }

            if (output == null) {
                break;
            }
        }
    }

    public abstract void operateOnce(BaseMachineRecipe output, List<ItemStack> processResult);

    public BaseMachineRecipe getOutput() {


        this.output = this.inputSlotA.process();

        return this.output;
    }

    public ContainerBase<? extends TileEntityTripleElectricMachine> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerTripleElectricMachine(entityPlayer, this, type);
    }

    public String getStartSoundFile() {
        return null;
    }

    public String getInterruptSoundFile() {
        return null;
    }

    public void onNetworkEvent(int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
        }

        switch (event) {
            case 0:
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
                break;
            case 1:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    if (this.getInterruptSoundFile() != null) {
                        IC2.audioManager.playOnce(
                                this,
                                PositionSpec.Center,
                                this.getInterruptSoundFile(),
                                false,
                                IC2.audioManager.getDefaultVolume()
                        );
                    }
                }
                break;
            case 2:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                }
            case 3:
        }

    }

    public double getEnergy() {
        return this.energy.getEnergy();
    }

    public boolean useEnergy(double amount) {
        return this.energy.useEnergy(amount);
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(
                UpgradableProperty.Processing,
                UpgradableProperty.Transformer,
                UpgradableProperty.EnergyStorage,
                UpgradableProperty.ItemConsuming,
                UpgradableProperty.ItemProducing
        );
    }

    public void onGuiClosed(EntityPlayer player) {
    }


    public final float getChargeLevel() {
        return Math.min((float) this.energy.getEnergy() / (float) this.energy.getCapacity(), 1);
    }

    public double getProgress() {
        return this.guiProgress;
    }

    public String getInventoryName() {
        return this.name;
    }


}
