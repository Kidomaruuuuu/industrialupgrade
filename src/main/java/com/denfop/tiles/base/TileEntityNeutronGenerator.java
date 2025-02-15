package com.denfop.tiles.base;

import com.denfop.Config;
import com.denfop.blocks.FluidName;
import com.denfop.container.ContainerNeutronGenerator;
import com.denfop.gui.GUINeutronGenerator;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquid.OpType;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Set;

@NotClassic
public class TileEntityNeutronGenerator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {

    private static final int DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/matterFabricatorTier");
    private final float energycost;

    private double lastEnergy;
    private AudioSource audioSource;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableLiquid containerslot;
    @GuiSynced
    public final FluidTank fluidTank;
    protected final Fluids fluids;

    public TileEntityNeutronGenerator() {
        super((int) (Config.energy * 128), 14);

        this.energycost = (float) Config.energy;
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.containerslot = new InvSlotConsumableLiquidByList(this, "container", Access.I, 1, InvSide.TOP, OpType.Fill,
                FluidName.fluidNeutron.getInstance()
        );
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 9 * 1000,
                Fluids.fluidPredicate(FluidName.fluidNeutron.getInstance())
        );

    }


    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.lastEnergy = nbt.getDouble("lastEnergy");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("lastEnergy", this.lastEnergy);
        return nbt;
    }

    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.setUpgradestat();
        }

    }

    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }

        super.onUnloaded();
    }

    protected void updateEntityServer() {
        super.updateEntityServer();

        boolean needsInvUpdate;
        needsInvUpdate = this.upgradeSlot.tickNoMark();
        if (!(this.energy.getEnergy() <= 0.0D)) {


            this.setActive(true);

            if (this.energy.getEnergy() >= this.energycost) {
                needsInvUpdate = this.attemptGeneration();
            }

            needsInvUpdate |= this.containerslot.processFromTank(this.fluidTank, this.outputSlot);
            this.lastEnergy = this.energy.getEnergy();
            if (needsInvUpdate) {
                this.markDirty();
                this.setActive(true);
            }
        } else {

            this.setActive(false);
        }

    }

    public boolean attemptGeneration() {
        int k = (int) (this.energy.getEnergy() / this.energycost);
        int m;

        if (this.fluidTank.getFluidAmount() + 1 > this.fluidTank.getCapacity()) {
            return false;
        }
        m = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
        this.fluidTank.fillInternal(new FluidStack(FluidName.fluidNeutron.getInstance(), Math.min(m, k)), true);
        this.energy.useEnergy(this.energycost * Math.min(m, k));
        return true;
    }

    public String getProgressAsString() {
        int p = Math.min((int) (this.energy.getEnergy() * 100.0D / this.energycost), 100);
        return "" + p + "%";
    }


    public ContainerBase<TileEntityNeutronGenerator> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerNeutronGenerator(entityPlayer, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GUINeutronGenerator(new ContainerNeutronGenerator(entityPlayer, this));
    }

    public void onGuiClosed(EntityPlayer player) {
    }


    public void onNetworkUpdate(String field) {


        super.onNetworkUpdate(field);
    }

    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setUpgradestat();
        }

    }

    public void setUpgradestat() {
        this.upgradeSlot.onChanged();
        this.energy.setSinkTier(applyModifier(this.upgradeSlot.extraTier));
    }

    private static int applyModifier(int extra) {
        double ret = (double) Math.round(((double) DEFAULT_TIER + (double) extra));
        return ret > 2.147483647E9D ? 2147483647 : (int) ret;
    }

    public double getEnergy() {
        return this.energy.getEnergy();
    }

    public boolean useEnergy(double amount) {
        return this.energy.useEnergy(amount);
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(
                UpgradableProperty.RedstoneSensitive,
                UpgradableProperty.Transformer,
                UpgradableProperty.ItemConsuming,
                UpgradableProperty.ItemProducing,
                UpgradableProperty.FluidProducing
        );
    }


}
