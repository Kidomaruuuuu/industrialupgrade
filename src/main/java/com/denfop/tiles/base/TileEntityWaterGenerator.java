package com.denfop.tiles.base;

import com.denfop.container.ContainerWaterGenerator;
import com.denfop.gui.GuiWaterGenerator;
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
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@NotClassic
public class TileEntityWaterGenerator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {

    private static final int DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/matterFabricatorTier");
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableLiquid containerslot;
    public final FluidTank fluidTank;
    protected final Fluids fluids;
    private final float energycost;
    private double lastEnergy;
    private AudioSource audioSource;

    public TileEntityWaterGenerator() {
        super(10000, 14, 1);

        this.energycost = 40;
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.containerslot = new InvSlotConsumableLiquidByList(this, "container", Access.I, 1, InvSide.TOP, OpType.Fill,
                FluidRegistry.WATER
        );
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 20 * 1000,
                Fluids.fluidPredicate(FluidRegistry.WATER)
        );

    }

    private static int applyModifier(int extra) {
        double ret = (double) Math.round(((double) DEFAULT_TIER + (double) extra));
        return ret > 2.147483647E9D ? 2147483647 : (int) ret;
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

    public void initiate(int soundEvent) {
        IC2.network.get(true).initiateTileEntityEvent(this, soundEvent, true);
    }

    protected void updateEntityServer() {
        super.updateEntityServer();

        boolean needsInvUpdate;
        needsInvUpdate = this.upgradeSlot.tickNoMark();
        if (!(this.energy.getEnergy() <= 0.0D) && this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity()) {


            this.setActive(true);

            if (this.energy.getEnergy() >= this.energycost) {
                needsInvUpdate = this.attemptGeneration();
                if(this.world.provider.getWorldTime() % 40 == 0)
                initiate(0);
            } else {
                if(this.world.provider.getWorldTime() % 40 == 0)
                initiate(2);
            }

            this.lastEnergy = this.energy.getEnergy();
            if (needsInvUpdate) {
                this.markDirty();
                this.setActive(true);
            }
        } else {

            this.setActive(false);
        }
        if(!this.containerslot.isEmpty() &&this.fluidTank.getFluidAmount() > 0)
            this.containerslot.processFromTank(this.fluidTank, this.outputSlot);

    }

    public boolean attemptGeneration() {
        int k = (int) (this.energy.getEnergy() / this.energycost);
        int m;

        if (this.fluidTank.getFluidAmount() + 1 > this.fluidTank.getCapacity()) {
            return false;
        }
        m = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
        this.fluidTank.fillInternal(new FluidStack(FluidRegistry.WATER, Math.min(m, k)), true);
        this.energy.useEnergy(this.energycost * Math.min(m, k));
        return true;
    }

    public String getProgressAsString() {
        int p = Math.min((int) (this.energy.getEnergy() * 100.0D / this.energycost), 100);
        return "" + p + "%";
    }

    public ContainerBase<TileEntityWaterGenerator> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerWaterGenerator(entityPlayer, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiWaterGenerator(new ContainerWaterGenerator(entityPlayer, this));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public List<String> getNetworkedFields() {

        return new ArrayList<>(super.getNetworkedFields());
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

    public String getStartSoundFile() {
        return null;
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }

    public void setUpgradestat() {
        this.upgradeSlot.onChanged();
        this.energy.setSinkTier(applyModifier(this.upgradeSlot.extraTier));
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
