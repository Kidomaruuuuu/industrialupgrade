package com.denfop.tiles.mechanism;

import com.denfop.IUItem;
import net.minecraft.block.Block;

@SuppressWarnings("SameParameterValue")
public enum EnumUpgradesMultiMachine {
    DOUBLE_MACERATOR("ic2.te.macerator", 0, IUItem.machines_base, 0, EnumTypeMachines.MACERATOR),
    DOUBLE_EXTRACTOR("ic2.te.extractor", 0, IUItem.machines_base, 9, EnumTypeMachines.EXTRACTOR),
    DOUBLE_FURNACE("ic2.te.electric_furnace", 0, IUItem.machines_base, 6, EnumTypeMachines.ELECTRICFURNACE),
    DOUBLE_COMPRESSOR("ic2.te.compressor", 0, IUItem.machines_base, 3, EnumTypeMachines.COMPRESSOR),
    DOUBLE_RECYLER("ic2.te.recycler", 0, IUItem.machines_base1, 0, EnumTypeMachines.RECYCLER),
    DOUBLE_METALFORMER("ic2.te.metal_former", 0, IUItem.machines_base, 12, EnumTypeMachines.METALFOMER);

    public final int meta_item;
    public final String name;
    public final Block block;
    public final int meta;
    public final EnumTypeMachines type;

    EnumUpgradesMultiMachine(String name, int meta_item, Block block, int meta, EnumTypeMachines type) {
        this.name = name;
        this.meta_item = meta_item;
        this.block = block;
        this.meta = meta;
        this.type = type;
    }

    public static void register() {
        for (EnumUpgradesMultiMachine value : values()) {
            IUItem.map4.put(value.name, value);
        }
    }
}
