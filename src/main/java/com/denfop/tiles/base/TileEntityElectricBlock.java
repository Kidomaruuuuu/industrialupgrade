package com.denfop.tiles.base;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.denfop.Config;
import com.denfop.api.IStorage;
import com.denfop.container.ContainerElectricBlock;
import com.denfop.gui.GuiElectricBlock;
import com.denfop.invslot.InvSlotElectricBlock;
import com.denfop.items.modules.AdditionModule;
import com.denfop.proxy.CommonProxy;
import com.denfop.tiles.panels.entity.TileEntitySolarPanel;
import com.denfop.tiles.wiring.EnumElectricBlock;
import com.denfop.utils.ModUtils;
import ic2.api.energy.EnergyNet;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.api.tile.IWrenchable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.EntityIC2FX;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class TileEntityElectricBlock extends TileEntityInventory implements IHasGui, IWrenchable,
        INetworkClientTileEntityEventListener, IEnergyHandler, IEnergyReceiver,
        IEnergyStorage, IEnergyProvider, IStorage {

    public final double tier;
    public final boolean chargepad;
    public final String name;
    public static EnumElectricBlock electricblock;
    public EntityPlayer player;

    public double output;

    public final Energy energy;
    public final double maxStorage2;
    public String UUID = null;
    public double energy2;
    public boolean rf;
    public boolean rfeu = false;
    public boolean needsInvUpdate = false;
    public boolean movementcharge = false;
    public boolean movementchargerf = false;
    public boolean movementchargeitemrf = false;
    public double output_plus;
    public final double l;
    public final InvSlotElectricBlock inputslotA;
    public final InvSlotElectricBlock inputslotB;
    public final InvSlotElectricBlock inputslotC;
    public short temp;

    public TileEntityElectricBlock(double tier1, double output1, double maxStorage1, boolean chargepad, String name) {

        this.energy2 = 0.0D;
        this.tier = tier1;
        this.output = output1;
        this.player = null;
        this.maxStorage2 = maxStorage1 * 4;
        this.chargepad = chargepad;
        this.rf = false;
        this.name = name;
        this.inputslotA = new InvSlotElectricBlock(this, 1, "input", 1);
        this.inputslotB = new InvSlotElectricBlock(this, 2, "input1", 1);
        this.inputslotC = new InvSlotElectricBlock(this, 3, "input2", 2);
        this.output_plus = 0;
        this.temp = 0;
        this.l = output1;
        this.energy = this.addComponent((new Energy(this, maxStorage1,
                EnumSet.complementOf(EnumSet.of(EnumFacing.DOWN)), EnumSet.of(EnumFacing.DOWN), (int) tier,
                EnergyNet.instance.getTierFromPower(this.output), false
        )));
        this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(EnumFacing.DOWN));

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final List<String> info, final ITooltipFlag advanced) {


        info.add(Localization.translate("ic2.item.tooltip.Output") + " " + ModUtils.getString(this.getOutput()) + " EU/t ");
        info.add(Localization.translate("iu.maxStoragestored") + " " + ModUtils.getString(this.energy.getCapacity()) + " EU ");
        info.add(Localization.translate("iu.maxStoragestored") + " " + ModUtils.getString(this.maxStorage2) + " RF ");
        NBTTagCompound nbttagcompound = ModUtils.nbt(itemStack);
        info.add(Localization.translate("ic2.item.tooltip.Capacity") + " " + ModUtils.getString(nbttagcompound.getDouble("energy"))
                + " EU ");
        info.add(Localization.translate("ic2.item.tooltip.Capacity") + " " + ModUtils.getString(nbttagcompound.getDouble("energy2"))
                + " RF ");
        info.add(Localization.translate("iu.tier") + ModUtils.getString(this.tier));


    }

    public TileEntityElectricBlock(EnumElectricBlock electricBlock) {
        this(electricBlock.tier, electricBlock.producing, electricBlock.maxstorage, electricBlock.chargepad, electricBlock.name1);
        electricblock = electricBlock;
    }

    public static EnumElectricBlock getElectricBlock() {

        return electricblock;
    }

    public ContainerBase<TileEntityElectricBlock> getGuiContainer(EntityPlayer player) {
        return new ContainerElectricBlock(player, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiElectricBlock(new ContainerElectricBlock(entityPlayer, this));
    }

    protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        List<String> list = new ArrayList<>();
        list.add(UUID);
        for (int h = 0; h < 2; h++) {
            if (inputslotC.get(h) != null && inputslotC.get(h).getItem() instanceof AdditionModule
                    && inputslotC.get(h).getItemDamage() == 0) {
                for (int m = 0; m < 9; m++) {
                    NBTTagCompound nbt = ModUtils.nbt(inputslotC.get(h));
                    String name = "player_" + m;
                    if (!nbt.getString(name).isEmpty()) {
                        list.add(nbt.getString(name));
                    }
                }
                break;
            }

        }
        if (personality) {
            if (!(list.contains(player.getDisplayName().getFormattedText()) || player.capabilities.isCreativeMode)) {
                CommonProxy.sendPlayerMessage(player, Localization.translate("iu.error"));
                return false;
            }
        }
        module_charge(player);
        return this.getWorld().isRemote || IC2.platform.launchGui(player, this);

    }

    protected void getItems(EntityPlayer player) {
        List<String> list = new ArrayList<>();
        list.add(UUID);
        for (int h = 0; h < 2; h++) {
            if (inputslotC.get(h) != null && inputslotC.get(h).getItem() instanceof AdditionModule
                    && inputslotC.get(h).getItemDamage() == 0) {
                for (int m = 0; m < 9; m++) {
                    NBTTagCompound nbt = ModUtils.nbt(inputslotC.get(h));
                    String name = "player_" + m;
                    if (!nbt.getString(name).isEmpty()) {
                        list.add(nbt.getString(name));
                    }
                }
                break;
            }

        }


        if (player != null) {
            if (personality) {
                if (!(list.contains(player.getDisplayName().getFormattedText()) || player.capabilities.isCreativeMode)) {
                    IC2.platform.messagePlayer(player, Localization.translate("iu.error"));
                    return;
                }
            }
            for (ItemStack current : player.inventory.armorInventory) {
                if (current != null) {
                    chargeitems(current, this.output);
                }
            }
            for (ItemStack current : player.inventory.mainInventory) {
                if (current != null) {
                    chargeitems(current, this.output);
                }
            }
            player.inventoryContainer.detectAndSendChanges();

        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        World world = this.getWorld();
        Random rnd = world.rand;
        if (rnd.nextInt(8) == 0) {
            if (this.getActive()) {
                ParticleManager effect = FMLClientHandler.instance().getClient().effectRenderer;

                for (int particles = 20; particles > 0; --particles) {
                    double x = (float) this.pos.getX() + 0.0F + rnd.nextFloat();
                    double y = (float) this.pos.getY() + 0.9F + rnd.nextFloat();
                    double z = (float) this.pos.getZ() + 0.0F + rnd.nextFloat();
                    effect.addEffect(new EntityIC2FX(
                            world,
                            x,
                            y,
                            z,
                            60,
                            new double[]{0.0D, 0.1D, 0.0D},
                            new float[]{0.2F, 0.2F, 1.0F}
                    ));
                }
            }

        }
    }

    protected void chargeitems(ItemStack itemstack, double chargefactor) {
        if (!(itemstack.getItem() instanceof ic2.api.item.IElectricItem || itemstack.getItem() instanceof IEnergyContainerItem)) {
            return;
        }
        if (this.energy2 > 0 && itemstack.getItem() instanceof IEnergyContainerItem) {
            double sent = 0;

            IEnergyContainerItem item = (IEnergyContainerItem) itemstack.getItem();
            double energy_temp = this.energy2;
            if (item.getEnergyStored(itemstack) < item.getMaxEnergyStored(itemstack)
                    && this.energy2 > 0) {
                sent = (sent + this.extractEnergy1(
                        item.receiveEnergy(itemstack, (int) this.energy2, false), false));

            }
            energy_temp -= (sent * 2);
            this.energy2 = energy_temp;


        }
        double freeamount = ElectricItem.manager.charge(itemstack, Double.POSITIVE_INFINITY, (int) this.tier, true, true);
        double charge;
        if (freeamount >= 0.0D) {
            charge = Math.min(freeamount, chargefactor);
            if (this.energy.getEnergy() < charge) {
                charge = this.energy.getEnergy();
            }
            this.energy.useEnergy(ElectricItem.manager.charge(itemstack, charge, (int) this.tier, true, false));
        }

    }


    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

        return receiveEnergy(maxReceive, simulate);

    }

    public int receiveEnergy(int paramInt, boolean paramBoolean) {
        int i = (int) Math.min(this.maxStorage2 - this.energy2, Math.min(this.output * 4, paramInt));
        if (!paramBoolean) {
            this.energy2 += i;
        }
        return i;
    }

    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return extractEnergy((int) Math.min(this.output * 4, maxExtract), simulate);
    }

    public int extractEnergy(int paramInt, boolean paramBoolean) {
        int i = (int) Math.min(this.energy2, Math.min(this.output * 4, paramInt));
        if (!paramBoolean) {
            this.energy2 -= i;
        }
        return i;
    }


    public float getChargeLevel() {

        float ret = (float) ((float) this.energy.getEnergy() / (this.energy.getCapacity()));

        if (ret > 1.0F) {
            ret = 1.0F;
        }
        return ret;
    }

    public float getChargeLevel1() {

        float ret = (float) ((float) this.energy2 / (this.maxStorage2));

        if (ret > 1.0F) {
            ret = 1.0F;
        }
        return ret;
    }

    public boolean canConnectEnergy(EnumFacing arg0) {
        return true;
    }

    public int getEnergyStored(EnumFacing from) {
        return (int) this.energy2;
    }

    public int getMaxEnergyStored(EnumFacing from) {
        return (int) this.maxStorage2;
    }


    public void module_charge(EntityPlayer entityPlayer) {

        if (this.movementcharge) {

            for (ItemStack armorcharged : entityPlayer.inventory.armorInventory) {
                if (armorcharged != null) {
                    if (armorcharged.getItem() instanceof IElectricItem && this.energy.getEnergy() > 0) {
                        double sent = ElectricItem.manager.charge(armorcharged, this.energy.getEnergy(), 2147483647, true,
                                false
                        );
                        entityPlayer.inventoryContainer.detectAndSendChanges();
                        this.energy.useEnergy(sent);

                        this.needsInvUpdate = (sent > 0.0D);
                        if (sent > 0) {
                            CommonProxy.sendPlayerMessage(
                                    entityPlayer,
                                    Localization.translate("successfully.charged")
                                            + armorcharged.getDisplayName()
                                            + Localization.translate("iu.sendenergy")
                                            + ModUtils.getString(sent) + " EU"
                            );
                        }

                    }

                }

            }

        }
        if (this.movementchargerf) {

            for (ItemStack charged : entityPlayer.inventory.armorInventory) {
                if (charged != null) {

                    if (charged.getItem() instanceof IEnergyContainerItem && this.energy2 > 0) {
                        double sent = 0;

                        IEnergyContainerItem item = (IEnergyContainerItem) charged.getItem();
                        double energy_temp = this.energy2;
                        while (item.getEnergyStored(charged) < item.getMaxEnergyStored(charged)
                                && this.energy2 > 0) {
                            sent = (sent + this.extractEnergy1(
                                    item.receiveEnergy(charged, (int) this.energy2, false), false));

                        }
                        energy_temp -= (sent);
                        this.energy2 = energy_temp;
                        if (sent > 0) {
                            CommonProxy.sendPlayerMessage(
                                    entityPlayer,
                                    Localization.translate("successfully.charged")
                                            + charged.getDisplayName()
                                            + Localization.translate("iu.sendenergy")
                                            + ModUtils.getString(sent) + " RF"
                            );
                        }
                        entityPlayer.inventoryContainer.detectAndSendChanges();

                    }

                }
            }
        }
        if (this.movementchargeitem) {
            for (ItemStack charged : entityPlayer.inventory.mainInventory) {
                if (charged != null) {
                    if (charged.getItem() instanceof IElectricItem && this.energy.getEnergy() > 0) {
                        double sent = ElectricItem.manager.charge(charged, this.energy.getEnergy(), 2147483647, true, false);

                        this.energy.useEnergy(sent);
                        this.needsInvUpdate = (sent > 0.0D);
                        if (sent > 0) {
                            CommonProxy.sendPlayerMessage(
                                    entityPlayer,
                                    Localization.translate("successfully.charged")
                                            + charged.getDisplayName()
                                            + Localization.translate("iu.sendenergy")
                                            + ModUtils.getString(sent) + " EU"
                            );
                        }
                        entityPlayer.inventoryContainer.detectAndSendChanges();

                    }

                }

            }

        }
        if (this.movementchargeitemrf) {
            for (ItemStack charged : entityPlayer.inventory.mainInventory) {
                if (charged != null) {

                    if (charged.getItem() instanceof IEnergyContainerItem && this.energy2 > 0) {
                        double sent = 0;

                        IEnergyContainerItem item = (IEnergyContainerItem) charged.getItem();
                        double energy_temp = this.energy2;
                        while (item.getEnergyStored(charged) < item.getMaxEnergyStored(charged)
                                && this.energy2 > 0) {
                            sent = (sent + this.extractEnergy1(
                                    item.receiveEnergy(charged, (int) this.energy2, false), false));

                        }
                        energy_temp -= (sent);
                        this.energy2 = energy_temp;
                        if (sent > 0) {
                            CommonProxy.sendPlayerMessage(
                                    entityPlayer,
                                    Localization.translate("successfully.charged")
                                            + charged.getDisplayName()
                                            + Localization.translate("iu.sendenergy")
                                            + ModUtils.getString(sent) + " RF"
                            );
                        }
                        entityPlayer.inventoryContainer.detectAndSendChanges();

                    }

                }
            }
        }
    }

    protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
        if (chargepad) {
            return Arrays.asList(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D));
        } else {
            return super.getAabbs(forCollision);
        }
    }

    protected void onEntityCollision(Entity entity) {
        super.onEntityCollision(entity);
        if (!this.getWorld().isRemote && entity instanceof EntityPlayer) {
            if (this.chargepad) {
                this.playerstandsat((EntityPlayer) entity);
            }
            if (player != null) {
                module_charge(player);
            }
        }

    }

    public void playerstandsat(EntityPlayer entity) {
        if (this.player == null) {
            this.player = entity;
        } else if (this.player.getUniqueID() != entity.getUniqueID()) {
            this.player = entity;
        }
    }

    protected boolean shouldEmitEnergy() {

        return true;

    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        this.energy.setSendingEnabled(this.shouldEmitEnergy());
        this.inputslotC.wirelessmodule();
        if (chargepad) {
            if (this.player != null && this.energy.getEnergy() >= 1.0D) {
                if (!getActive()) {
                    setActive(true);
                }
                getItems(this.player);
                this.player = null;
                needsInvUpdate = true;
            } else if (getActive()) {
                setActive(false);
                needsInvUpdate = true;
            }
        }
        if (this.UUID != null) {
            personality = this.inputslotC.personality();
        }
        this.output_plus = this.inputslotC.output_plus(this.l);
        this.output = this.l + this.output_plus;
        this.movementcharge = this.inputslotC.getstats().get(0);
        this.movementchargeitem = this.inputslotC.getstats().get(1);
        this.movementchargerf = this.inputslotC.getstats().get(2);
        this.movementchargeitemrf = this.inputslotC.getstats().get(3);

        this.rf = this.inputslotC.getstats().get(4);
        if (this.rf) {
            if (!this.rfeu) {
                if (energy.getEnergy() > 0 && energy2 < maxStorage2) {

                    energy2 += energy.getEnergy() * Config.coefficientrf;
                    energy.useEnergy(energy.getEnergy());

                }
                if (energy2 > maxStorage2) {
                    double rf = (energy2 - maxStorage2);
                    energy.addEnergy(rf / Config.coefficientrf);
                    energy2 = maxStorage2;
                }
            } else {

                if (energy2 > 0 && energy.getEnergy() < energy.getCapacity()) {
                    energy2 -= energy.addEnergy(energy2 / Config.coefficientrf) * Config.coefficientrf;

                }

            }
        }
        IEnergyContainerItem item;
        if (this.energy2 >= 1.0D && this.inputslotA.get(0) != null
                && this.inputslotA.get(0).getItem() instanceof IEnergyContainerItem) {
            item = (IEnergyContainerItem) this.inputslotA.get(0).getItem();
            if (item.getEnergyStored(this.inputslotA.get(0)) < item.getMaxEnergyStored(this.inputslotA.get(0))) {
                extractEnergy1(
                        item.receiveEnergy(this.inputslotA.get(0), (int) this.energy2, false),
                        false
                );
            }
        }
        if (this.energy.getEnergy() >= this.energy.getCapacity()) {
            this.energy.addEnergy(this.energy.getCapacity() - this.energy.getEnergy());
        }
        if (this.energy.getEnergy() < 0) {
            this.energy.addEnergy(-this.energy.getEnergy());
        }
        if (this.energy2 < 0) {
            this.energy2 = 0;
        }
        if (this.energy2 >= this.maxStorage2) {
            this.energy2 = this.maxStorage2;
        }
        if (!this.inputslotA.isEmpty()) {
            boolean ignore = this.inputslotC.checkignore();
            if (this.inputslotA.charge(
                    this.energy.getEnergy() > 1D ? this.energy.getEnergy() : 0,
                    this.inputslotA.get(0),
                    true,ignore
            ) != 0) {
                this.energy.useEnergy(this.inputslotA.charge(this.energy.getEnergy() > 1D ? this.energy.getEnergy() : 0,
                        this.inputslotA.get(0), false,ignore
                ));
                needsInvUpdate = ((this.energy.getEnergy() > 1D ? this.energy.getEnergy() : 0) > 0.0D);
            }
        }
        if (this.inputslotB.get(0) != null) {
            if (this.inputslotB.discharge(
                    this.energy.getEnergy() < this.energy.getCapacity() ? this.energy.getEnergy() : 0,
                    this.inputslotB.get(0),
                    true
            ) != 0) {

                this.energy.addEnergy(this.inputslotB.discharge(this.energy.getEnergy() < this.energy.getCapacity() ?
                        this.energy.getEnergy() : 0, this.inputslotB.get(0), false));
                needsInvUpdate = ((this.energy.getEnergy() > 1D ? this.energy.getEnergy() : 0) > 0.0D);
            }
        }
        if (this.rf) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos pos = new BlockPos(
                        this.pos.getX() + facing.getFrontOffsetX(),
                        this.pos.getY() + facing.getFrontOffsetY(),
                        this.pos.getZ() + facing.getFrontOffsetZ()
                );

                if (this.getWorld().getTileEntity(pos) == null) {
                    continue;
                }
                TileEntity tile = this.getWorld().getTileEntity(pos);

                if (!(tile instanceof TileEntitySolarPanel)) {

                    if (tile instanceof IEnergyReceiver) {
                        extractEnergy(facing, ((IEnergyReceiver) tile).receiveEnergy(facing.getOpposite(),
                                extractEnergy(facing, (int) this.energy2, true), false
                        ), false);
                    }
                }
            }
        }
        if (needsInvUpdate) {
            markDirty();
        }
    }

    public int getCapacity() {
        return (int) this.energy.getCapacity();
    }

    @Override
    public double getEUCapacity() {
        return this.energy.getCapacity();
    }

    @Override
    public double getRFCapacity() {
        return this.maxStorage2;
    }

    public int getOutput() {
        return (int) (this.output + this.output_plus);
    }

    public double getOutputEnergyUnitsPerTick() {
        return this.output + this.output_plus;
    }

    @Override
    public int getStored() {
        return (int) this.energy.getEnergy();
    }

    public void setStored(int energy1) {

    }

    public int addEnergy(int amount) {
        this.energy.addEnergy(amount);
        return amount;
    }


    public double extractEnergy1(double maxExtract, boolean simulate) {
        double temp;

        temp = this.energy2;

        if (temp > 0) {
            double energyExtracted = Math.min(temp, maxExtract);
            if (!simulate &&
                    this.energy2 - temp >= 0.0D) {
                this.energy2 -= temp;
                if (energyExtracted > 0) {
                    temp -= energyExtracted;
                    this.energy2 += temp;
                }
                return energyExtracted;
            }
        }
        return 0;
    }


    public boolean movementchargeitem = false;

    public boolean personality = false;

    public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!(getWorld()).isRemote) {
            NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            this.energy.addEnergy(nbt.getDouble("energy"));
            this.energy2 = nbt.getDouble("energy2");
        }
    }

    public void onPlaced(double eustored, double eustored1, EntityPlayer player, EnumFacing side) {
        super.onPlaced(null, player, side);
        if (!(getWorld()).isRemote) {
            this.energy.addEnergy(eustored);
            this.energy2 = eustored1;
        }
    }

    protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
        drop = super.adjustDrop(drop, wrench);
        if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self) {
            double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
            double totalEnergy = this.energy.getEnergy();
            if (retainedRatio > 0.0D && totalEnergy > 0.0D) {
                NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
                nbt.setDouble("energy", Math.round(totalEnergy * retainedRatio));
                nbt.setDouble("energy2", Math.round(this.energy2 * retainedRatio));

            }
        }
        return drop;
    }


    @Override
    public List<ItemStack> getWrenchDrops(
            World world,
            BlockPos blockPos,
            IBlockState iBlockState,
            TileEntity tileEntity,
            EntityPlayer entityPlayer,
            int i
    ) {
        List<ItemStack> list = new ArrayList<>();
        return list;
    }


    @Override
    public boolean canSetFacing(World world, BlockPos pos, EnumFacing enumFacing, EntityPlayer player) {
        if (!this.teBlock.allowWrenchRotating()) {
            return false;
        } else if (enumFacing == this.getFacing()) {
            return false;
        } else {
            return this.getSupportedFacings().contains(enumFacing);
        }
    }

    @Override
    public EnumFacing getFacing(World world, BlockPos blockPos) {
        return this.getFacing();
    }

    @Override
    public boolean setFacing(World world, BlockPos blockPos, EnumFacing enumFacing, EntityPlayer entityPlayer) {
        if (!this.canSetFacingWrench(enumFacing, entityPlayer)) {
            return false;
        } else {
            this.setFacing(enumFacing);
            return true;
        }
    }

    @Override
    public boolean wrenchCanRemove(World world, BlockPos blockPos, EntityPlayer entityPlayer) {
        return true;
    }


    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.energy.setDirections(
                EnumSet.complementOf(EnumSet.of(this.getFacing(), EnumFacing.UP)),
                EnumSet.of(this.getFacing())
        );
        if (nbttagcompound.getString("UUID") != null) {
            this.UUID = nbttagcompound.getString("UUID");
        }
        this.temp = nbttagcompound.getShort("temp");

        if (((temp >> 6) & 1) == 1) {
            this.movementchargeitemrf = true;
        }
        if (((temp >> 5) & 1) == 1) {
            this.movementchargeitem = true;
        }
        if (((temp >> 4) & 1) == 1) {
            this.movementcharge = true;
        }
        if (((temp >> 3) & 1) == 1) {
            this.movementchargerf = true;
        }
        if ((temp & 1) == 1) {
            this.personality = true;
        }

        if (((temp >> 2) & 1) == 1) {
            this.rfeu = true;
        }
        if (((temp >> 1) & 1) == 1) {
            this.rf = true;
        }

        this.energy2 = Util.limit(nbttagcompound.getDouble("energy2"), 0.0D,
                this.maxStorage2
        );

    }

    public void setFacing(EnumFacing facing) {
        super.setFacing(facing);
        this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));

    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        if (energy2 > 0) {
            nbttagcompound.setDouble("energy2", this.energy2);
        }
        this.temp = (short) (this.movementchargeitemrf ? 1 : 0);
        this.temp = (short) ((this.temp << 1) + (short) (this.movementchargeitem ? 1 : 0));
        this.temp = (short) ((this.temp << 1) + (short) (this.movementcharge ? 1 : 0));
        this.temp = (short) ((this.temp << 1) + (short) (this.movementchargerf ? 1 : 0));
        this.temp = (short) ((this.temp << 1) + (short) (this.rfeu ? 1 : 0));
        this.temp = (short) ((this.temp << 1) + (short) (this.rf ? 1 : 0));
        this.temp = (short) ((this.temp << 1) + (short) (this.personality ? 1 : 0));
        nbttagcompound.setShort("temp", temp);


        if (this.UUID != null) {
            nbttagcompound.setString("UUID", this.UUID);
        }

        return nbttagcompound;
    }


    public boolean isTeleporterCompatible(EnumFacing side) {
        return true;
    }

    public void onGuiClosed(EntityPlayer player) {
    }


    public void onNetworkEvent(EntityPlayer player, int event) {
        this.rfeu = !this.rfeu;

    }


    public String getInventoryName() {
        return Localization.translate(this.name);
    }


    public void onUpgraded() {
        this.rerender();
    }

}
