package com.denfop.tiles.base;

import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.denfop.Config;
import com.denfop.IUCore;
import com.denfop.IUItem;
import com.denfop.api.audio.EnumTypeAudio;
import com.denfop.api.audio.IAudioFixer;
import com.denfop.audio.AudioSource;
import com.denfop.audio.PositionSpec;
import com.denfop.componets.AdvEnergy;
import com.denfop.componets.CoolComponent;
import com.denfop.componets.EXPComponent;
import com.denfop.componets.ProcessMultiComponent;
import com.denfop.componets.RFComponent;
import com.denfop.container.ContainerMultiMachine;
import com.denfop.gui.GuiMultiMachine;
import com.denfop.items.modules.ItemModuleTypePanel;
import com.denfop.tiles.mechanism.EnumTypeMachines;
import com.denfop.tiles.panels.entity.EnumSolarPanels;
import com.denfop.tiles.panels.entity.TileEntitySolarPanel;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.init.Localization;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class TileEntityMultiMachine extends TileEntityInventory implements IHasGui, IEnergyHandler, IEnergyReceiver,
        IAudioFixer, IUpgradableBlock , INetworkClientTileEntityEventListener {


    public final int type;
    public final AdvEnergy energy;
    public final InvSlotDischarge dischargeSlot;
    public final CoolComponent cold;
    public final ProcessMultiComponent multi_process;
    public EXPComponent exp;
    public EnumSolarPanels solartype;
    public RFComponent energy2;
    public AudioSource audioSource;
    public EnumTypeAudio typeAudio = EnumTypeAudio.OFF;
    public EnumTypeAudio[] valuesAudio = EnumTypeAudio.values();
    private int tick;
    public final int sizeWorkingSlot;

    public TileEntityMultiMachine(int energyconsume, int OperationsPerTick, int type) {
        this(1, energyconsume, OperationsPerTick, type);
    }



    public TileEntityMultiMachine(
            int aDefaultTier,
            int energyconsume,
            int OperationsPerTick,
            int type
    ) {

        this.sizeWorkingSlot = this.getMachine().sizeWorkingSlot;
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, aDefaultTier, false, InvSlot.InvSide.ANY);
        this.energy = this.addComponent(AdvEnergy
                .asBasicSink(this, (double) energyconsume * OperationsPerTick, (int) Math.pow(2,this.sizeWorkingSlot-1))
                .addManagedSlot(this.dischargeSlot));

        this.energy2 = this.addComponent(new RFComponent(this, energyconsume * OperationsPerTick * 4, energy));
        this.type = type;
        this.solartype = null;
        this.cold = this.addComponent(CoolComponent.asBasicSink(this, 100));


        this.exp = null;
        if (this.getMachine().type == EnumTypeMachines.ELECTRICFURNACE) {
            this.exp = this.addComponent(EXPComponent.asBasicSource(this, 5000, 14));
        }
        this.multi_process = this.addComponent(new ProcessMultiComponent(this,getMachine()));
    }



    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("press.lshift"));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("iu.multimachine.info"));
            tooltip.add(Localization.translate("iu.machines_work_energy") + this.multi_process.defaultEnergyConsume + Localization.translate("iu.machines_work_energy_type_eu"));
            tooltip.add(Localization.translate("iu.machines_work_length") + this.multi_process.defaultOperationLength);
        }
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

    public void onUpdate() {

    }

    public CoolComponent getComponent() {
        return this.cold;
    }

    public List<ItemStack> getDrop() {

        return getAuxDrops(0);

    }

    public List<ItemStack> getWrenchDrops(EntityPlayer player, int fortune) {
        List<ItemStack> ret = super.getWrenchDrops(player, fortune);
        ItemStack stack_rf = ItemStack.EMPTY;
        ItemStack stack_quickly = ItemStack.EMPTY;
        ItemStack stack_modulesize = ItemStack.EMPTY;
        ItemStack stack_modulestorage = ItemStack.EMPTY;
        ItemStack panel = ItemStack.EMPTY;
        ItemStack colling = ItemStack.EMPTY;
        if (this.energy2.isRf()) {
            stack_rf = new ItemStack(IUItem.module7, 1, 4);
        }
        if (this.multi_process.quickly) {
            stack_quickly = new ItemStack(IUItem.module_quickly);
        }
        if (this.multi_process.modulesize) {
            stack_modulesize = new ItemStack(IUItem.module_stack);
        }
        if (this.multi_process.modulestorage) {
            stack_modulestorage = new ItemStack(IUItem.module_storage);
        }
        if (solartype != null) {
            panel = new ItemStack(IUItem.module6, 1, solartype.meta);
        }
        if (solartype != null) {
            panel = new ItemStack(IUItem.module6, 1, solartype.meta);
        }
        if (this.cold.upgrade) {
            colling = new ItemStack(IUItem.coolupgrade, 1, this.cold.meta);

        }
        if (!stack_modulestorage.isEmpty() || !stack_rf.isEmpty() || !stack_quickly.isEmpty() || !stack_modulesize.isEmpty() || !panel.isEmpty() || !colling.isEmpty()) {
            if (!stack_rf.isEmpty()) {
                ret.add(stack_rf);
                this.multi_process.shrinkModule(1);
                this.energy2.setRf(false);
            }
            if (!stack_modulestorage.isEmpty()) {
                ret.add(stack_modulestorage);
                this.multi_process.shrinkModule(1);
                this.multi_process.setModulesize(false);
            }
            if (!stack_quickly.isEmpty()) {
                ret.add(stack_quickly);
                this.multi_process.shrinkModule(1);
                this.multi_process.setQuickly(false);
            }
            if (!stack_modulesize.isEmpty()) {
                ret.add(stack_modulesize);
                this.multi_process.shrinkModule(1);
                this.multi_process.setModulesize(false);
            }
            if (solartype != null) {
                ret.add(panel);
                solartype = null;
            }
            if (!colling.isEmpty()) {
                ret.add(colling);
                this.cold.upgrade = false;
                this.cold.meta = 0;
            }
        }
        return ret;
    }

    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

      return   this.energy2.receiveEnergy(from,maxReceive,simulate);

    }


    public void updateVisibility(TileEntitySolarPanel type) {
        type.wetBiome = this.world.getBiome(this.pos).getRainfall() > 0.0F;
        type.noSunWorld = this.world.provider.isNether();

        type.rain = type.wetBiome && (this.world.isRaining() || this.world.isThundering());
        type.sunIsUp = this.world.isDaytime();
        type.skyIsVisible = this.world.canBlockSeeSky(this.pos.up()) &&
                (this.world.getBlockState(this.pos.up()).getMaterial().getMaterialMapColor() ==
                        MapColor.AIR) && !type.noSunWorld;
        if (!type.skyIsVisible) {
            type.active = TileEntitySolarPanel.GenerationState.NONE;
        }
        if (type.sunIsUp && type.skyIsVisible) {
            if (!(type.rain)) {
                type.active = TileEntitySolarPanel.GenerationState.DAY;
            } else {
                type.active = TileEntitySolarPanel.GenerationState.RAINDAY;
            }

        }
        if (!type.sunIsUp && type.skyIsVisible) {
            if (!(type.rain)) {
                type.active = TileEntitySolarPanel.GenerationState.NIGHT;
            } else {
                type.active = TileEntitySolarPanel.GenerationState.RAINNIGHT;
            }
        }
        if (type.getWorld().provider.getDimension() == 1) {
            type.active = TileEntitySolarPanel.GenerationState.END;
        }
        if (type.getWorld().provider.getDimension() == -1) {
            type.active = TileEntitySolarPanel.GenerationState.NETHER;
        }

    }

    @Override
    protected boolean onActivated(
            final EntityPlayer entityPlayer,
            final EnumHand hand,
            final EnumFacing side,
            final float hitX,
            final float hitY,
            final float hitZ
    ) {

        if (!entityPlayer.getHeldItem(hand).isEmpty()) {
            if (entityPlayer.getHeldItem(hand).getItem() instanceof ItemModuleTypePanel) {
                if (this.solartype != null) {
                    EnumSolarPanels type = this.solartype;
                    int meta = type.meta;
                    ItemStack stack = new ItemStack(IUItem.module6, 1, meta);
                    if (!entityPlayer.inventory.addItemStackToInventory(stack)) {
                        EntityItem item = new EntityItem(
                                entityPlayer.getEntityWorld(),
                                (int) (entityPlayer.posX),
                                (int) entityPlayer.posY - 1,
                                (int) (entityPlayer.posZ)
                        );
                        item.setItem(stack);
                        item.setPosition(entityPlayer.posX, entityPlayer.posY - 1, entityPlayer.posZ);
                        item.setDefaultPickupDelay();
                        world.spawnEntity(item);
                    }

                }
                this.solartype = ItemModuleTypePanel.getSolarType(entityPlayer.getHeldItem(hand).getItemDamage());
                entityPlayer.getHeldItem(hand).setCount(entityPlayer.getHeldItem(hand).getCount() - 1);
                return true;
            }
            if( this.multi_process.onActivated(entityPlayer.getHeldItem(hand)))
                return true;

        }
        return super.onActivated(entityPlayer, hand, side, hitX, hitY, hitZ);
    }


    public boolean canConnectEnergy(EnumFacing arg0) {
        return true;
    }

    public int getEnergyStored(EnumFacing from) {
        return (int) this.energy2.getEnergy();
    }

    public int getMaxEnergyStored(EnumFacing from) {
        return (int) this.energy2.getCapacity();
    }



    public abstract EnumMultiMachine getMachine();

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        int id = nbttagcompound.getInteger("panelid");
        if (id != -1) {
            this.solartype = IUItem.map1.get(id);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        if (this.solartype != null) {
            nbttagcompound.setInteger("panelid", this.solartype.meta);
        } else {
            nbttagcompound.setInteger("panelid", -1);
        }
        return nbttagcompound;
    }
    public void onNetworkEvent(EntityPlayer player, int event) {
        if (event == 0) {
            this.multi_process.cycleMode();
        }
    }

    protected void onLoaded() {
        super.onLoaded();

    }



    public EnumTypeMachines getTypeMachine() {
        return this.getMachine().type;
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
        IC2.platform.isSimulating();

    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        if (solartype != null) {
            if (this.energy.getEnergy() < this.energy.getCapacity() || (energy2.getEnergy() < energy2.getCapacity() && this.energy2.isRf())) {
                TileEntitySolarPanel panel = new TileEntitySolarPanel(solartype);
                if (panel.getWorld() != this.getWorld()) {
                    panel.setWorld(this.getWorld());
                }
                panel.skyIsVisible = this.world.canBlockSeeSky(this.pos.up()) &&
                        (this.world.getBlockState(this.pos.up()).getMaterial().getMaterialMapColor() ==
                                MapColor.AIR) && !panel.noSunWorld;
                panel.wetBiome = panel.getWorld().getBiome(this.pos).getRainfall() > 0.0F;
                panel.rain = panel.wetBiome && (this.world.isRaining() || this.world.isThundering());
                panel.sunIsUp = this.getWorld().isDaytime();

                if (panel.active == null || this.getWorld().provider.getWorldTime() % 40 == 0) {
                    updateVisibility(panel);
                }
                panel.gainFuel();
                if (this.energy.getEnergy() < this.energy.getCapacity()) {
                    this.energy.addEnergy(Math.min(panel.generating, energy.getFreeEnergy()));
                } else if (this.energy2.getEnergy() < energy2.getCapacity() && this.energy2.isRf()) {
                    energy2.addEnergy(Math.min(panel.generating,
                            (this.energy2.getCapacity() - this.energy2.getEnergy()) / Config.coefficientrf));
                }
            }
        }
        if (!getActive()) {
            this.tick++;

            if (this.tick % 120 == 0) {
                this.cold.useEnergy(0.35);
                this.tick = 0;
            }
        } else {
            tick = 0;
        }

    }






    public ContainerBase<? extends TileEntityMultiMachine> getGuiContainer(EntityPlayer player) {
        return new ContainerMultiMachine(player, this, this.sizeWorkingSlot);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {

            return new GuiMultiMachine(new ContainerMultiMachine(player, this, sizeWorkingSlot));



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
                if (this.audioSource != null && !this.audioSource.playing()) {
                    this.audioSource.play();
                }
                break;
            case 1:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    boolean stop = true;
                    for (int i = 0; i < sizeWorkingSlot; i++) {
                        if (this.multi_process.output[i] != null) {
                            stop = false;
                            break;
                        }
                    }
                    if (stop) {
                        if (this.getInterruptSoundFile() != null) {
                            IUCore.audioManager.playOnce(
                                    this,
                                    PositionSpec.Center,
                                    this.getInterruptSoundFile(),
                                    false,
                                    IUCore.audioManager.getDefaultVolume()
                            );
                        }
                    } else {
                        if (this.audioSource != null && !this.audioSource.playing()) {
                            this.audioSource.play();
                        }
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

    public int getMode() {
        return 0;
    }

}
