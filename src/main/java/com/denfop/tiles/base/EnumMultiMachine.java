package com.denfop.tiles.base;

import com.denfop.IUItem;
import com.denfop.tiles.mechanism.EnumTypeMachines;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum EnumMultiMachine {

    DOUBLE_MACERATOR(
            "Macerator Double",
            2,
            300,
            2,
            IUItem.machines_base,
            1,
            1,
            0,
            EnumTypeMachines.MACERATOR,
            IUItem.machines_base
    ),
    TRIPLE_MACERATOR(
            "Macerator Triple",
            2,
            300,
            3,
            IUItem.machines_base,
            2,
            2,
            1,
            EnumTypeMachines.MACERATOR,
            IUItem.machines_base
    ),
    QUAD_MACERATOR("Macerator Quad", 2, 300, 4, IUItem.machines_base, 3, -1, 2, EnumTypeMachines.MACERATOR, IUItem.machines_base),

    DOUBLE_COMPRESSER(
            "Compressor Double",
            2,
            300,
            2,
            IUItem.machines_base,
            4,
            1,
            3,
            EnumTypeMachines.COMPRESSOR,
            IUItem.machines_base
    ),
    TRIPLE_COMPRESSER(
            "Compressor Triple",
            2,
            300,
            3,
            IUItem.machines_base,
            5,
            2,
            4,
            EnumTypeMachines.COMPRESSOR,
            IUItem.machines_base
    ),
    QUAD_COMPRESSER(
            "Compressor Quad",
            2,
            300,
            4,
            IUItem.machines_base,
            6,
            -1,
            5,
            EnumTypeMachines.COMPRESSOR,
            IUItem.machines_base
    ),

    DOUBLE_EXTRACTOR(
            "Extractor Double",
            2,
            300,
            2,
            IUItem.machines_base,
            10,
            1,
            9,
            EnumTypeMachines.EXTRACTOR,
            IUItem.machines_base
    ),
    TRIPLE_EXTRACTOR(
            "Extractor Triple",
            2,
            300,
            3,
            IUItem.machines_base,
            11,
            2,
            10,
            EnumTypeMachines.EXTRACTOR,
            IUItem.machines_base
    ),
    QUAD_EXTRACTOR(
            "Extractor Quad",
            2,
            300,
            4,
            IUItem.machines_base,
            12,
            -1,
            11,
            EnumTypeMachines.EXTRACTOR,
            IUItem.machines_base
    ),

    DOUBLE_ELECTRIC_FURNACE(
            "Double Electric Furnace",
            3,
            100,
            2,
            IUItem.machines_base,
            7,
            1,
            6,
            EnumTypeMachines.ELECTRICFURNACE, IUItem.machines_base
    ),
    TRIPLE_ELECTRIC_FURNACE(
            "Triple Electric Furnace",
            3,
            100,
            3,
            IUItem.machines_base,
            8,
            2,
            7,
            EnumTypeMachines.ELECTRICFURNACE,
            IUItem.machines_base
    ),
    QUAD_ELECTRIC_FURNACE(
            "Quad Electric Furnace",
            3,
            100,
            4,
            IUItem.machines_base,
            9,
            -1,
            8,
            EnumTypeMachines.ELECTRICFURNACE,
            IUItem.machines_base
    ),

    DOUBLE_METAL_FORMER(
            "Metal Former Double",
            10,
            200,
            2,
            IUItem.machines_base,
            13,
            1,
            12,
            EnumTypeMachines.METALFOMER,
            IUItem.machines_base
    ),
    TRIPLE_METAL_FORMER(
            "Metal Former Triple",
            10,
            200,
            3,
            IUItem.machines_base,
            14,
            2,
            13,
            EnumTypeMachines.METALFOMER,
            IUItem.machines_base
    ),
    QUAD_METAL_FORMER(
            "Metal Former Quad",
            10,
            200,
            4,
            IUItem.machines_base,
            15,
            -1,
            14,
            EnumTypeMachines.METALFOMER,
            IUItem.machines_base
    ),

    DOUBLE_RECYCLER(
            "Double Recycler",
            1,
            45,
            2,
            IUItem.machines_base1,
            1,
            1,
            0,
            EnumTypeMachines.RECYCLER,
            IUItem.machines_base1
    ),
    TRIPLE_RECYCLER(
            "Triple Recycler",
            1,
            45,
            3,
            IUItem.machines_base1,
            2,
            2,
            1,
            EnumTypeMachines.RECYCLER,
            IUItem.machines_base1
    ),
    QUAD_RECYCLER("Quad Recycler", 1, 45, 4, IUItem.machines_base1, 3, -1, 2, EnumTypeMachines.RECYCLER, IUItem.machines_base1),

    DOUBLE_COMB_RECYCLER(
            "Double Combined  Recycler",
            1,
            45,
            2,
            IUItem.machines_base1,
            4,
            1,
            3,
            EnumTypeMachines.COMBRECYCLER,
            IUItem.machines_base1
    ),
    TRIPLE_COMB_RRECYCLER(
            "Triple Combined  Recycler",
            1,
            45,
            3,
            IUItem.machines_base1,
            5,
            2,
            4,
            EnumTypeMachines.COMBRECYCLER,
            IUItem.machines_base1
    ),
    QUAD_COMB_RRECYCLER(
            "Quad Combined  Recycler",
            1,
            45,
            4,
            IUItem.machines_base1,
            6,
            -1,
            5,
            EnumTypeMachines.COMBRECYCLER,
            IUItem.machines_base1
    ),
    COMB_MACERATOR(
            "Combined Macerator",
            2,
            300,
            1,
            IUItem.machines_base1,
            7,
            0,
            6,
            EnumTypeMachines.COMBMACERATOR,
            IUItem.machines_base1
    ),
    COMB_DOUBLE_MACERATOR(
            "Combined Macerator Double",
            2,
            300,
            2,
            IUItem.machines_base1,
            8,
            1,
            7,
            EnumTypeMachines.COMBMACERATOR, IUItem.machines_base1
    ),
    COMB_TRIPLE_MACERATOR(
            "Combined Macerator Triple",
            2,
            300,
            3,
            IUItem.machines_base1,
            9,
            2,
            8,
            EnumTypeMachines.COMBMACERATOR, IUItem.machines_base1
    ),
    COMB_QUAD_MACERATOR(
            "Combined Macerator Quad",
            2,
            300,
            4,
            IUItem.machines_base1,
            10,
            -1,
            9,
            EnumTypeMachines.COMBMACERATOR,
            IUItem.machines_base1
    ),

    Rolling("Rolling", 10, 200, 1, IUItem.machines_base2, 1, 0, 0, EnumTypeMachines.ROLLING, IUItem.machines_base2),
    DOUBLE_Rolling(
            " Double Rolling",
            10,
            200,
            2,
            IUItem.machines_base2,
            2,
            1,
            1,
            EnumTypeMachines.ROLLING,
            IUItem.machines_base2
    ),
    TRIPLE_Rolling(
            " Triple Rolling",
            10,
            200,
            3,
            IUItem.machines_base2,
            3,
            2,
            2,
            EnumTypeMachines.ROLLING,
            IUItem.machines_base2
    ),
    QUAD_Rolling("Quad Rolling", 10, 200, 4, IUItem.machines_base2, 4, -1, 3, EnumTypeMachines.ROLLING, IUItem.machines_base2),
    Extruding("Extruding", 10, 200, 1, IUItem.machines_base2, 5, 0, 4, EnumTypeMachines.EXTRUDING, IUItem.machines_base2),
    DOUBLE_Extruding(
            "Double Extruding ",
            10,
            200,
            2,
            IUItem.machines_base2,
            6,
            1,
            5,
            EnumTypeMachines.EXTRUDING,
            IUItem.machines_base2
    ),
    TRIPLE_Extruding(
            "Triple Extruding",
            10,
            200,
            3,
            IUItem.machines_base2,
            7,
            2,
            6,
            EnumTypeMachines.EXTRUDING,
            IUItem.machines_base2
    ),
    QUAD_Extruding(
            "Quad Extruding",
            10,
            200,
            4,
            IUItem.machines_base2,
            8,
            -1,
            7,
            EnumTypeMachines.EXTRUDING,
            IUItem.machines_base2
    ),
    Cutting("Cutting", 10, 200, 1, IUItem.machines_base2, 9, 0, 8, EnumTypeMachines.CUTTING, IUItem.machines_base2),
    DOUBLE_Cutting(
            "Double Cutting",
            10,
            200,
            2,
            IUItem.machines_base2,
            10,
            1,
            9,
            EnumTypeMachines.CUTTING,
            IUItem.machines_base2
    ),
    TRIPLE_Cutting(
            "Triple Cutting",
            10,
            200,
            3,
            IUItem.machines_base2,
            11,
            2,
            10,
            EnumTypeMachines.CUTTING,
            IUItem.machines_base2
    ),
    QUAD_Cutting("Quad Cutting", 10, 200, 4, IUItem.machines_base2, 12, -1, 11, EnumTypeMachines.CUTTING, IUItem.machines_base2),

    Fermer("Fermer", 4, 5000, 1, IUItem.machines_base3, 1, 0, 0, EnumTypeMachines.FARMER, IUItem.machines_base3),
    DOUBLE_Fermer("Double Fermer", 4, 5000, 2, IUItem.machines_base3, 2, 1, 1, EnumTypeMachines.FARMER, IUItem.machines_base3),
    TRIPLE_Fermer("Triple Fermer", 4, 5000, 3, IUItem.machines_base3, 3, 2, 2, EnumTypeMachines.FARMER, IUItem.machines_base3),
    QUAD_Fermer("Quad Fermer", 4, 5000, 4, IUItem.machines_base3, 4, -1, 3, EnumTypeMachines.FARMER, IUItem.machines_base3),

    AssamplerScrap(
            "AssamplerScrap",
            1,
            25,
            1,
            IUItem.machines_base3,
            5,
            0,
            4,
            EnumTypeMachines.ASSAMPLERSCRAP,
            IUItem.machines_base3
    ),
    DOUBLE_AssamplerScrap(
            "Double AssamplerScrap",
            1,
            25,
            2,
            IUItem.machines_base3,
            6,
            1,
            5,
            EnumTypeMachines.ASSAMPLERSCRAP,
            IUItem.machines_base3
    ),
    TRIPLE_AssamplerScrap(
            "Triple AssamplerScrap",
            1,
            25,
            3,
            IUItem.machines_base3,
            7,
            2,
            6,
            EnumTypeMachines.ASSAMPLERSCRAP,
            IUItem.machines_base3
    ),
    QUAD_AssamplerScrap(
            "Quad AssamplerScrap",
            1,
            25,
            4,
            IUItem.machines_base3,
            8,
            -1,
            7,
            EnumTypeMachines.ASSAMPLERSCRAP,
            IUItem.machines_base3
    ),
    MACERATOR("Macerator", 2, 300, 1, IUItem.machines_base, 0, 0, 0, EnumTypeMachines.MACERATOR, IUItem.simplemachine),

    COMPRESSER("Compressor", 2, 300, 1, IUItem.machines_base, 3, 0, 1, EnumTypeMachines.COMPRESSOR, IUItem.simplemachine),

    EXTRACTOR("Extractor", 2, 300, 1, IUItem.machines_base, 9, 0, 3, EnumTypeMachines.EXTRACTOR, IUItem.simplemachine),

    ELECTRIC_FURNACE(
            "Electric Furnace",
            3,
            100,
            1,
            IUItem.machines_base,
            6,
            0,
            2,
            EnumTypeMachines.ELECTRICFURNACE, IUItem.simplemachine
    ),

    METAL_FORMER("Metal Former", 10, 200, 1, IUItem.machines_base, 12, 0, 4, EnumTypeMachines.METALFOMER, IUItem.simplemachine),

    RECYCLER("Recycler", 1, 45, 1, IUItem.machines_base1, 0, 0, 5, EnumTypeMachines.RECYCLER, IUItem.simplemachine),
    ;
    public final int usagePerTick;
    public final int lenghtOperation;
    public final int sizeWorkingSlot;
    public final Block block_new;
    public final int meta_new;
    public final int upgrade;
    public final String name;
    public final int meta;
    public final EnumTypeMachines type;
    public final String recipe;
    public final Block block;

    EnumMultiMachine(
            String name,
            int usagePerTick,
            int lenghtOperation,
            int sizeWorkingSlot,
            Block block_new,
            int meta_new,
            int upgrade, int meta, EnumTypeMachines type, Block block
    ) {

        this.name = name;
        this.usagePerTick = usagePerTick;
        this.lenghtOperation = lenghtOperation;
        this.sizeWorkingSlot = sizeWorkingSlot;
        this.block_new = block_new;
        this.meta_new = meta_new;
        this.upgrade = upgrade;
        this.meta = meta;
        this.type = type;
        this.recipe = type.recipe;
        this.block = block;
    }

    public static void write() {
        IUItem.map_upgrades = new HashMap<>();
        for (EnumMultiMachine multiMachine : values()) {
            if (multiMachine.upgrade != -1) {
                if (!IUItem.map_upgrades.containsKey(multiMachine.upgrade)) {
                    List<ItemStack> list = new ArrayList<>();
                    list.add(new ItemStack(multiMachine.block, 1, multiMachine.meta));
                    IUItem.map_upgrades.put(multiMachine.upgrade, list);
                } else {
                    IUItem.map_upgrades.get(multiMachine.upgrade).add(new ItemStack(multiMachine.block, 1, multiMachine.meta));

                }
            }
        }
        final ItemStack stack = new ItemStack(IUItem.oilquarry);
        IUItem.map_upgrades.get(0).add(stack);
        IUItem.map_upgrades.get(1).add(stack);
        IUItem.map_upgrades.get(2).add(stack);
        List<ItemStack> list1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<ItemStack> list = IUItem.map_upgrades.get(i);
            list.forEach(stack2 -> {
                if (!list1.contains(stack2)) {
                    list1.add(stack2);
                }
            });
        }
        list1.add(stack);
        IUItem.map_upgrades.put(3, list1);
        List<ItemStack> list2 = new ArrayList<>(IUItem.map_upgrades.get(3));
        list2.removeIf(stack1 -> stack1.isItemEqual(stack));
        for (EnumMultiMachine multiMachine : values()) {
            if (multiMachine.upgrade == -1) {
                list2.add(new ItemStack(multiMachine.block, 1, multiMachine.meta));
            }
        }
        IUItem.upgrades_panels = list2;
    }

}
