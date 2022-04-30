package com.denfop.invslot;

import com.denfop.IUItem;
import com.denfop.items.modules.EnumQuarryModules;
import com.denfop.items.modules.EnumQuarryType;
import com.denfop.items.modules.ItemQuarryModule;
import com.denfop.tiles.base.TileEntityBaseQuantumQuarry;
import com.denfop.utils.ModUtils;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.item.ItemStack;

public class InvSlotQuantumQuarry extends InvSlot {

    public final int type;
    public final TileEntityBaseQuantumQuarry tile;
    public int stackSizeLimit;

    public InvSlotQuantumQuarry(TileEntityBaseQuantumQuarry base1, int oldStartIndex1, String name, int type) {
        super(base1, name, Access.I, oldStartIndex1, InvSlot.InvSide.TOP);
        this.tile = base1;
        this.stackSizeLimit = 1;
        this.type = type;
    }

    public void update() {

        switch (this.type) {
            case 0:
                this.tile.comb_mac_enabled = false;
                this.tile.mac_enabled = false;
                this.tile.col = 1;
                this.tile.chance = 0;
                this.tile.furnace = false;
                if (!this.isEmpty()) {
                    ItemStack type1 = this.get();
                    EnumQuarryModules module = EnumQuarryModules.getFromID(type1.getItemDamage());
                    EnumQuarryType type = module.type;

                    switch (type) {
                        case SPEED:
                            break;
                        case DEPTH:
                            this.tile.col = module.efficiency * module.efficiency;
                            break;
                        case LUCKY:
                            this.tile.chance = module.efficiency;
                            break;
                        case FURNACE:
                            this.tile.furnace = true;
                            break;
                        case COMB_MAC:
                            this.tile.comb_mac_enabled = true;
                            break;
                        case MACERATOR:
                            this.tile.mac_enabled = true;
                            break;
                    }
                    this.tile.consume = this.tile.energyconsume * (1 + module.cost);
                } else {

                    this.tile.consume = this.tile.energyconsume;
                    this.tile.col = 1;
                    this.tile.chance = 0;
                    this.tile.furnace = false;
                    this.tile.comb_mac_enabled = false;
                    this.tile.mac_enabled = false;
                }
                break;
            case 1:
                if (this.get().isEmpty()) {
                    this.tile.list_modules = null;
                } else {
                    this.tile.list_modules = EnumQuarryModules.getFromID(this.get().getItemDamage());
                }
                this.tile.list = ModUtils.getListFromModule(this.get());
                break;
            case 2:
                this.tile.analyzer = !this.get().isEmpty();
                break;
        }
    }

    @Override
    public void put(final int index, final ItemStack content) {
        super.put(index, content);
        switch (this.type) {
            case 0:
                this.tile.comb_mac_enabled = false;
                this.tile.mac_enabled = false;
                this.tile.col = 1;
                this.tile.chance = 0;
                this.tile.furnace = false;
                if (!this.isEmpty()) {
                    ItemStack type1 = this.get();
                    EnumQuarryModules module = EnumQuarryModules.getFromID(type1.getItemDamage());
                    EnumQuarryType type = module.type;

                    switch (type) {
                        case SPEED:
                            break;
                        case DEPTH:
                            this.tile.col = module.efficiency * module.efficiency;
                            break;
                        case LUCKY:
                            this.tile.chance = module.efficiency;
                            break;
                        case FURNACE:
                            this.tile.furnace = true;
                            break;
                        case COMB_MAC:
                            this.tile.comb_mac_enabled = true;
                            break;
                        case MACERATOR:
                            this.tile.mac_enabled = true;
                            break;
                    }
                    this.tile.consume = this.tile.energyconsume * (1 + module.cost);
                } else {

                    this.tile.consume = this.tile.energyconsume;
                    this.tile.col = 1;
                    this.tile.chance = 0;
                    this.tile.furnace = false;
                }
                break;
            case 1:
                if (this.get().isEmpty()) {
                    this.tile.list_modules = null;
                } else {
                    this.tile.list_modules = EnumQuarryModules.getFromID(this.get().getItemDamage());
                }
                this.tile.list = ModUtils.getListFromModule(this.get());
                break;
            case 2:
                this.tile.analyzer = !this.get().isEmpty();
                break;
        }
    }

    public boolean accepts(ItemStack itemStack) {
        if (type == 0) {

            return itemStack.getItem() instanceof ItemQuarryModule && (EnumQuarryModules.getFromID(itemStack.getItemDamage()).type != EnumQuarryType.WHITELIST && EnumQuarryModules.getFromID(
                    itemStack.getItemDamage()).type != EnumQuarryType.BLACKLIST);
        } else if (type == 1) {
            if (itemStack.getItem() instanceof ItemQuarryModule && (EnumQuarryModules.getFromID(itemStack.getItemDamage()).type == EnumQuarryType.WHITELIST || EnumQuarryModules.getFromID(
                    itemStack.getItemDamage()).type == EnumQuarryType.BLACKLIST)) {
                ((TileEntityBaseQuantumQuarry) this.base).list = ModUtils.getListFromModule(itemStack);
                return true;
            }

        }
        return itemStack.getItem().equals(IUItem.analyzermodule);
    }

    public int getStackSizeLimit() {
        return this.stackSizeLimit;
    }

    public void setStackSizeLimit(int stackSizeLimit) {
        this.stackSizeLimit = stackSizeLimit;
    }

}
