package com.denfop.api.windsystem;

import com.denfop.api.windsystem.upgrade.EnumInfoRotorUpgradeModules;
import com.denfop.api.windsystem.upgrade.IRotorUpgradeItem;
import com.denfop.api.windsystem.upgrade.RotorUpgradeItemInform;
import com.denfop.api.windsystem.upgrade.RotorUpgradeSystem;
import com.denfop.api.windsystem.upgrade.event.EventRotorItemLoad;
import com.denfop.items.ItemRotorsUpgrade;
import com.denfop.utils.ModUtils;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;

public class InvSlotUpgrade extends InvSlot {

    private final IWindUpgradeBlock tile;

    public InvSlotUpgrade(IWindUpgradeBlock base1) {
        super((TileEntityInventory) base1, "upgrade_slot", InvSlot.Access.I, 4, InvSide.ANY);
        this.setStackSizeLimit(1);
        this.tile = base1;
    }

    @Override
    public boolean accepts(final ItemStack stack) {
        if (this.tile.getRotor() == null) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemRotorsUpgrade)) {
            return false;
        }
        final List<RotorUpgradeItemInform> list = RotorUpgradeSystem.instance.getInformation(
                this.tile.getItemStack());
        EnumInfoRotorUpgradeModules enumInfoRotorUpgradeModules = EnumInfoRotorUpgradeModules.getFromID(stack.getItemDamage());
        final RotorUpgradeItemInform modules = RotorUpgradeSystem.instance.getModules(enumInfoRotorUpgradeModules, list);
        if (modules == null) {
            return true;
        }
        return modules.number < enumInfoRotorUpgradeModules.getMax();
    }

    public void update() {
        for (int i = 0; i < this.size(); i++) {
            this.put(i, ItemStack.EMPTY, false);
        }
    }

    public void update(ItemStack stack) {
        final Map<Integer, ItemStack> map = RotorUpgradeSystem.instance.getList(stack);
        for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue(), false);
        }
    }


    public void put(final int index, final ItemStack content, boolean updates) {
        super.put(index, content);

    }

    @Override
    public void put(final int index, final ItemStack content) {
        super.put(index, content);


        if (content.isEmpty()) {
            if (!tile.getItemStack().isEmpty()) {
                RotorUpgradeSystem.instance.removeUpdate(tile.getItemStack(), base.getParent().getWorld(), index);
            }
        } else {
            final NBTTagCompound nbt = ModUtils.nbt(tile.getItemStack());
            nbt.setString("mode_module" + index, EnumInfoRotorUpgradeModules.getFromID(content.getItemDamage()).name);
            MinecraftForge.EVENT_BUS.post(new EventRotorItemLoad(
                    base.getParent().getWorld(),
                    (IRotorUpgradeItem) tile.getItemStack().getItem(),
                    tile.getItemStack()
            ));

        }
    }

}
