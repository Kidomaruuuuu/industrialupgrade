package com.denfop.tabs;


import com.denfop.IUItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class IUTab extends CreativeTabs {

    private final int type;

    public IUTab(int type, String name) {
        super(name);
        this.type = type;
    }

    @Nonnull
    public ItemStack getTabIconItem() {
        switch (type) {
            case 0:
                return new ItemStack(IUItem.blockpanel);
            case 1:
                return new ItemStack(IUItem.basemodules);
            case 2:
                return new ItemStack(IUItem.basecircuit, 1, 11);
            case 3:
                return new ItemStack(IUItem.toriyore);
            case 4:
                return new ItemStack(IUItem.quantumHelmet);
            case 5:
                return new ItemStack(IUItem.block);
            case 6:
                return IUItem.reactormendeleviumQuad;
            case 7:
                return new ItemStack(IUItem.machinekit, 1, 3);

        }
        return new ItemStack(IUItem.blockpanel);
    }

}
