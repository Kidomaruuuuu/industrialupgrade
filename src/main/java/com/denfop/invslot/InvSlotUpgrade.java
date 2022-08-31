package com.denfop.invslot;

import com.denfop.Ic2Items;
import com.denfop.api.recipe.InvSlotOutput;
import com.denfop.blocks.FluidName;
import ic2.api.upgrade.IAugmentationUpgrade;
import ic2.api.upgrade.IEnergyStorageUpgrade;
import ic2.api.upgrade.IFullUpgrade;
import ic2.api.upgrade.IProcessingUpgrade;
import ic2.api.upgrade.IRedstoneSensitiveUpgrade;
import ic2.api.upgrade.ITransformerUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.Redstone.IRedstoneModifier;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvSlotUpgrade extends InvSlot {

    private final TileEntityBlock tile;
    private Fluids fluids = null;
    private List<Fluids.InternalFluidTank> fluidTankList = new ArrayList<>();
    public int augmentation;
    public int extraProcessTime;
    public double processTimeMultiplier;
    public int extraEnergyDemand;
    public double energyDemandMultiplier;
    public int extraEnergyStorage;
    public double energyStorageMultiplier;
    public int extraTier;
    InvSlotOutput slot;
    public int tick = 0;
    private final Map<EnumFacing, IItemHandler> iItemHandlerMap = new HashMap<>();
    private final Map<EnumFacing, IFluidHandler> iFluidHandlerMap = new HashMap<>();
    private List<IRedstoneModifier> redstoneModifiers = Collections.emptyList();
    private boolean ejectorUpgrade;
    private boolean fluidEjectorUpgrade;
    public boolean update = false;
    public InvSlotUpgrade(
            IInventorySlotHolder base,
            String name,
            int count
    ) {
        super(base, name, Access.I, count);
        this.resetRates();


        try {
            slot = (InvSlotOutput) base.getInventorySlot("output");

        } catch (Exception e) {
            slot = null;
        }
        fluids = base.getParent().getComponent(Fluids.class);
        if(fluids != null)
        fluids.getAllTanks().forEach(fluidTankList::add);
        this.tile = (TileEntityBlock) base;
    }

    public void setSlot(final InvSlotOutput slot) {
        this.slot = slot;
    }

    private static int applyModifier(int base, int extra, double multiplier) {
        double ret = (double) Math.round(((double) base + (double) extra) * multiplier);
        return ret > 2.147483647E9D ? 2147483647 : (int) ret;
    }

    private static EnumFacing getDirection(ItemStack stack) {
        int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
        return rawDir >= 1 && rawDir <= 6 ? EnumFacing.VALUES[rawDir - 1] : null;
    }

    public boolean accepts(ItemStack stack) {
        Item rawItem = stack.getItem();
        if (!(rawItem instanceof IUpgradeItem)) {
            return false;
        } else {
            IUpgradeItem item = (IUpgradeItem) rawItem;
            return item.isSuitableFor(stack, ((IUpgradableBlock) this.base).getUpgradableProperties());
        }
    }

    public void onChanged() {
        this.resetRates();
        IUpgradableBlock block = (IUpgradableBlock) this.base;
        List<IRedstoneModifier> newRedstoneModifiers = new ArrayList<>();

        for (int i = 0; i < this.size(); ++i) {
            ItemStack stack = this.get(i);
            if (!StackUtil.isEmpty(stack)) {
                IUpgradeItem upgrade = (IUpgradeItem) stack.getItem();
                boolean all = upgrade instanceof IFullUpgrade;
                int size = StackUtil.getSize(stack);
                if (all || upgrade instanceof IAugmentationUpgrade) {
                    this.augmentation += ((IAugmentationUpgrade) upgrade).getAugmentation(stack, block) * size;
                }

                if (all || upgrade instanceof IProcessingUpgrade) {
                    IProcessingUpgrade procUpgrade = (IProcessingUpgrade) upgrade;
                    this.extraProcessTime += procUpgrade.getExtraProcessTime(stack, block) * size;
                    this.processTimeMultiplier *= Math.pow(procUpgrade.getProcessTimeMultiplier(stack, block), size);
                    this.extraEnergyDemand += procUpgrade.getExtraEnergyDemand(stack, block) * size;
                    this.energyDemandMultiplier *= Math.pow(procUpgrade.getEnergyDemandMultiplier(stack, block), size);
                }

                if (all || upgrade instanceof IEnergyStorageUpgrade) {
                    IEnergyStorageUpgrade engUpgrade = (IEnergyStorageUpgrade) upgrade;
                    this.extraEnergyStorage += engUpgrade.getExtraEnergyStorage(stack, block) * size;
                    this.energyStorageMultiplier *= Math.pow(engUpgrade.getEnergyStorageMultiplier(stack, block), size);
                }

                if (all || upgrade instanceof ITransformerUpgrade) {
                    this.extraTier += ((ITransformerUpgrade) upgrade).getExtraTier(stack, block) * size;
                }

                if (all || upgrade instanceof IRedstoneSensitiveUpgrade) {
                    IRedstoneSensitiveUpgrade redUpgrade = (IRedstoneSensitiveUpgrade) upgrade;
                    if (redUpgrade.modifiesRedstoneInput(stack, block)) {
                        newRedstoneModifiers.add(new InvSlotUpgrade.UpgradeRedstoneModifier(redUpgrade, stack, block));
                    }
                }
            }

        }

        for (final TileEntityComponent component : this.base.getParent().getComponents()) {
            if (component instanceof Redstone) {
                Redstone rs = (Redstone) component;
                rs.removeRedstoneModifiers(this.redstoneModifiers);
                rs.addRedstoneModifiers(newRedstoneModifiers);
                rs.update();
            }
        }
        this.redstoneModifiers = newRedstoneModifiers.isEmpty() ? Collections.emptyList() : newRedstoneModifiers;
        for (int i = 0; i < this.size(); ++i) {
            ItemStack stack = this.get(i);
        if (stack.isItemEqual(Ic2Items.ejectorUpgrade)) {
            this.ejectorUpgrade = true;
        } else if (stack.isItemEqual(Ic2Items.fluidEjectorUpgrade)) {
            this.fluidEjectorUpgrade = true;
        }
        }
    }

    private void resetRates() {
        this.augmentation = 0;
        this.extraProcessTime = 0;
        this.processTimeMultiplier = 1.0D;
        this.extraEnergyDemand = 0;
        this.energyDemandMultiplier = 1.0D;
        this.extraEnergyStorage = 0;
        this.energyStorageMultiplier = 1.0D;
        this.extraTier = 0;
        this.ejectorUpgrade =false;
        this.fluidEjectorUpgrade=false;
    }

    public int getOperationsPerTick(int defaultOperationLength) {
        return defaultOperationLength == 0 ? 64 : this.getOpsPerTick(this.getStackOpLen(defaultOperationLength));
    }

    public int getOperationLength(int defaultOperationLength) {
        if (defaultOperationLength == 0) {
            return 1;
        } else {
            double stackOpLen = this.getStackOpLen(defaultOperationLength);
            int opsPerTick = this.getOpsPerTick(stackOpLen);
            return Math.max(1, (int) Math.round(stackOpLen * (double) opsPerTick / 64.0D));
        }
    }

    private double getStackOpLen(int defaultOperationLength) {
        return ((double) defaultOperationLength + (double) this.extraProcessTime) * 64.0D * this.processTimeMultiplier;
    }

    private int getOpsPerTick(double stackOpLen) {
        return (int) Math.min(Math.ceil(64.0D / stackOpLen), 2.147483647E9D);
    }

    public int getEnergyDemand(int defaultEnergyDemand) {
        return applyModifier(defaultEnergyDemand, this.extraEnergyDemand, this.energyDemandMultiplier);
    }

    public int getEnergyStorage(int defaultEnergyStorage) {
        return applyModifier(defaultEnergyStorage, this.extraEnergyStorage, this.energyStorageMultiplier);
    }

    public int getTier(int defaultTier) {
        return applyModifier(defaultTier, this.extraTier, 1.0D);
    }

    public boolean tickNoMark() {
        IUpgradableBlock block = (IUpgradableBlock) this.base;
        boolean ret = false;
        this.tick++;
        if(this.iItemHandlerMap.isEmpty()){
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos pos = this.tile.getPos().offset(facing);
                final TileEntity tile1 = this.tile.getWorld().getTileEntity(pos);
                final IItemHandler handler = getItemHandler(tile1, facing.getOpposite());
                this.iItemHandlerMap.put(facing, handler);
            }
            this.iFluidHandlerMap.clear();
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos pos = this.tile.getPos().offset(facing);
                final TileEntity tile1 = this.tile.getWorld().getTileEntity(pos);
                final IFluidHandler handler = getFluidHandler(tile1, facing.getOpposite());
                this.iFluidHandlerMap.put(facing, handler);
            }
        }
        if(this.tick % 20 == 0) {
            if (this.ejectorUpgrade) {
                this.iItemHandlerMap.clear();
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos pos = this.tile.getPos().offset(facing);
                    final TileEntity tile1 = this.tile.getWorld().getTileEntity(pos);
                    final IItemHandler handler = getItemHandler(tile1, facing.getOpposite());
                    this.iItemHandlerMap.put(facing, handler);
                }
            }
            if (this.fluidEjectorUpgrade) {
                this.iFluidHandlerMap.clear();
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos pos = this.tile.getPos().offset(facing);
                    final TileEntity tile1 = this.tile.getWorld().getTileEntity(pos);
                    final IFluidHandler handler = getFluidHandler(tile1, facing.getOpposite());
                    this.iFluidHandlerMap.put(facing, handler);
                }
            }
            this.tick = 0;
        }
        boolean update = false;
        for (int i = 0; i < this.size(); ++i) {
            ItemStack stack = this.get(i);
            if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem) {
                update = true;
                if (stack.isItemEqual(Ic2Items.ejectorUpgrade)) {
                    this.tick(stack);
                } else if (stack.isItemEqual(Ic2Items.fluidEjectorUpgrade)) {
                    this.tick_fluid(stack);
                }

                else {
                    ((IUpgradeItem) stack.getItem()).onTick(stack, block);
                }
                ret = true;
            }
        }
        if(this.update != update){
            this.update = update;
            return true;
        }
        return ret;
    }

    public IItemHandler getItemHandler(@Nullable TileEntity tile, EnumFacing side) {
        if (tile == null) {
            return null;
        }

        IItemHandler handler = tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) ? tile.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                side
        ) : null;

        if (handler == null) {
            if (side != null && tile instanceof ISidedInventory) {
                handler = new SidedInvWrapper((ISidedInventory) tile, side);
            } else if (tile instanceof IInventory) {
                handler = new InvWrapper((IInventory) tile);
            }
        }

        return handler;
    }
    public IFluidHandler getFluidHandler(@Nullable TileEntity tile, EnumFacing side) {
        if (tile == null) {
            return null;
        }

        return tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side) ? tile.getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                side
        ) : null;
    }
    public boolean canItemStacksStack(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        if (a.isEmpty() || !a.isItemEqual(b) || a.hasTagCompound() != b.hasTagCompound()) {
            return false;
        }

        return (!a.hasTagCompound() || a.getTagCompound().equals(b.getTagCompound()));
    }

    private void tick(ItemStack stack) {
        EnumFacing facing = getDirection(stack);
        if (facing != null) {

            final IItemHandler handler = this.iItemHandlerMap.get(facing);
            if (handler == null) {
                return;
            }
            final int slots = handler.getSlots();
            if(this.slot != null)
            for (int j = 0; j < this.slot.size(); j++) {
                ItemStack took = this.slot.get(j);
                if (took.isEmpty()) {
                    continue;
                }
                took = took.copy();
                if (!(handler instanceof ISidedInventory)) {

                    if (insertItem(handler, took, true, slots).isEmpty()) {
                        this.slot.put(j, ItemStack.EMPTY);
                        insertItem(handler, took, false, slots);
                    }
                } else {
                    if (insertItem1(handler, took, true, slots).isEmpty()) {
                        this.slot.put(j, ItemStack.EMPTY);
                        insertItem1(handler, took, false, slots);

                    }
                }

            }
        } else {
            for (EnumFacing facing1 : EnumFacing.values()) {
                final IItemHandler handler =  this.iItemHandlerMap.get(facing1);
                if (handler == null) {
                    continue;
                }
                final int slots = handler.getSlots();
                if(this.slot != null)
                for (int j = 0; j < this.slot.size(); j++) {
                    ItemStack took = this.slot.get(j);
                    if (took.isEmpty()) {
                        continue;
                    }
                    took = took.copy();
                    if (!(handler instanceof ISidedInventory)) {

                        if (insertItem(handler, took, true, slots).isEmpty()) {
                            this.slot.put(j, ItemStack.EMPTY);
                            insertItem(handler, took, false, slots);
                        }
                    } else {
                        if (insertItem1(handler, took, true, slots).isEmpty()) {
                            this.slot.put(j, ItemStack.EMPTY);
                            insertItem1(handler, took, false, slots);

                        }
                    }

                }
            }
        }
    }
    private void tick_fluid(ItemStack stack) {
        EnumFacing facing = getDirection(stack);
        if (facing != null) {

            final IFluidHandler handler = this.iFluidHandlerMap.get(facing);
            if (handler == null) {
                return;
            }
            if(this.fluids == null)
                return;
            for(Fluids.InternalFluidTank tank : this.fluidTankList){
                if(tank.getFluidAmount() <= 0)
                    continue;

                int amount = handler.fill(tank.getFluid(),false);
                if(amount > 0 && tank.canDrain(facing)){
                    tank.drain(handler.fill(tank.getFluid(),true),true);
                }
            }
        } else {
            for (EnumFacing facing1 : EnumFacing.values()) {
                final IFluidHandler handler = this.iFluidHandlerMap.get(facing1);
                 if (handler == null) {
                    continue;
                }

                if(this.fluids == null)
                    return;
                for(Fluids.InternalFluidTank tank : this.fluidTankList){
                    if(tank.getFluidAmount() <= 0)
                        continue;
                    int amount = handler.fill(tank.getFluid(),false);
                    if(amount > 0 && tank.canDrain(facing1)){
                        tank.drain(handler.fill(tank.getFluid(),true),true);
                    }
                }
            }
        }
    }
    @Nonnull
    public ItemStack insertItem1(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate, int slot) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        for (int i = 0; i < slot; i++) {
            stack = this.insertItem2(dest, i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    @Nonnull
    public ItemStack insertItem2(IItemHandler dest, int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }


        ItemStack stackInSlot = dest.getStackInSlot(slot);

        int m;
        if (!stackInSlot.isEmpty()) {
            int max = stackInSlot.getMaxStackSize();
            int limit = dest.getSlotLimit(slot);
            if (stackInSlot.getCount() >= Math.min(max, limit)) {
                return stack;
            }

            if (!canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }


            m = Math.min(max, limit) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    ((SidedInvWrapper) dest).setStackInSlot(slot, copy);
                    return ItemStack.EMPTY;
                }

            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    copy.grow(stackInSlot.getCount());
                    ((SidedInvWrapper) dest).setStackInSlot(slot, copy);
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        } else {


            m = Math.min(stack.getMaxStackSize(), dest.getSlotLimit(slot));
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ((SidedInvWrapper) dest).setStackInSlot(slot, stack.splitStack(m));
                }
                return stack;
            } else {
                if (!simulate) {
                    ((SidedInvWrapper) dest).setStackInSlot(slot, stack);
                }
                return ItemStack.EMPTY;
            }
        }

    }

    @Nonnull
    public ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate, final int slots) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        for (int i = 0; i < slots; i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }



    private static class UpgradeRedstoneModifier implements IRedstoneModifier {

        private final IRedstoneSensitiveUpgrade upgrade;
        private final ItemStack stack;
        private final IUpgradableBlock block;

        UpgradeRedstoneModifier(IRedstoneSensitiveUpgrade upgrade, ItemStack stack, IUpgradableBlock block) {
            this.upgrade = upgrade;
            this.stack = stack.copy();
            this.block = block;
        }

        public int getRedstoneInput(int redstoneInput) {
            return this.upgrade.getRedstoneInput(this.stack, this.block, redstoneInput);
        }

    }

}
