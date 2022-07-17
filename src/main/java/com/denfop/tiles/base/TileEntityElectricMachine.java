package com.denfop.tiles.base;

import com.denfop.IUCore;
import com.denfop.api.audio.EnumTypeAudio;
import com.denfop.api.audio.IAudioFixer;
import com.denfop.api.recipe.InvSlotOutput;
import com.denfop.audio.AudioSource;
import com.denfop.audio.PositionSpec;
import com.denfop.componets.AdvEnergy;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class TileEntityElectricMachine extends TileEntityInventory implements IHasGui, IAudioFixer {


    public int tier;
    public double guiChargeLevel = 0;
    public AudioSource audioSource;


    public InvSlotOutput outputSlot = null;

    public AdvEnergy energy = null;
    public InvSlotDischarge dischargeSlot;
    public EnumTypeAudio typeAudio = EnumTypeAudio.OFF;
    public EnumTypeAudio[] valuesAudio = EnumTypeAudio.values();

    public TileEntityElectricMachine(double MaxEnergy, int tier, int count) {

        this.tier = tier;
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, false, InvSlot.InvSide.ANY);
        if (MaxEnergy != 0) {
            energy = this.addComponent(AdvEnergy.asBasicSink(this, MaxEnergy, tier).addManagedSlot(this.dischargeSlot));
        }

        if (count != 0) {
            this.outputSlot = new InvSlotOutput(this, "output", count);
        }
        if (MaxEnergy != 0) {
            this.guiChargeLevel = this.energy.getFillRatio();
        }
        valuesAudio = EnumTypeAudio.values();

    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
        if (this.hasComponent(AdvEnergy.class)) {
            AdvEnergy energy = this.getComponent(AdvEnergy.class);
            if (!energy.getSourceDirs().isEmpty()) {
                tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSourceTier()));
            } else if (!energy.getSinkDirs().isEmpty()) {
                tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSinkTier()));
            }
        }

    }

    public EnumTypeAudio getType() {
        return typeAudio;
    }

    public void setType(EnumTypeAudio type) {
        typeAudio = type;
    }

    public void initiate(int soundEvent) {
        if (this.getType() == valuesAudio[soundEvent % valuesAudio.length]) {
            return;
        }
        setType(valuesAudio[soundEvent % valuesAudio.length]);
        IC2.network.get(true).initiateTileEntityEvent(this, soundEvent, true);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);


    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);


        return nbttagcompound;
    }


    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IUCore.audioManager.removeSources(this);
            this.audioSource = null;
        }

    }

    public void markDirty() {
        super.markDirty();


    }

    protected void updateEntityServer() {
        super.updateEntityServer();


    }


    public String getStartSoundFile() {
        return null;
    }

    public String getInterruptSoundFile() {
        return null;
    }

    public void onNetworkEvent(int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IUCore.audioManager.createSource(this, this.getStartSoundFile());
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
                        IUCore.audioManager.playOnce(
                                this,
                                PositionSpec.Center,
                                this.getInterruptSoundFile(),
                                false,
                                IUCore.audioManager.getDefaultVolume()
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


    public void onGuiClosed(EntityPlayer player) {
    }


    public float getChargeLevel() {
        return (float) Math.min(1, this.energy.getEnergy() / this.energy.getCapacity());
    }


}
