package com.denfop.api.windsystem.upgrade;

import com.denfop.IUItem;

import java.util.ArrayList;
import java.util.List;

public enum EnumInfoRotorUpgradeModules {
    STRENGTH_I(1, 4, 0.05), // 0
    STRENGTH_II(2, 4, 0.1), // 1
    STRENGTH_III(3, 4, 0.15), // 2
    EFFICIENCY_I(1, 2, 0.05),// 3
    EFFICIENCY_II(2, 2, 0.1), // 4
    EFFICIENCY_III(3, 2, 0.15),//5
    POWER_I(1, 4, 0.1),//6
    POWER_II(2, 4, 0.15),//7
    POWER_III(3, 4, 0.2),//8
    AUTO(1, 1, 1),//9
    MIN(1, 1, 1),//10

    ;

    public final String name;
    public final List<Integer> list;
    public final int max;
    private final int level;
    private final double coef;

    EnumInfoRotorUpgradeModules(int level, int max, double coef) {
        this.name = this.name().toLowerCase();
        this.list = new ArrayList<>();
        this.level = level;
        this.max = max;
        this.coef = coef;
        IUItem.list1.add(this.name);
    }

    public static EnumInfoRotorUpgradeModules getFromID(final int ID) {
        return values()[ID % values().length];
    }

    public double getCoef() {
        return coef;
    }

    public int getMax() {
        return max;
    }

    public int getLevel() {
        return level;
    }

}
