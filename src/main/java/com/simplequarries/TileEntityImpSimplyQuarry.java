package com.simplequarries;

import com.denfop.componets.EnumTypeStyle;

public class TileEntityImpSimplyQuarry extends TileEntityBaseQuarry {

    public TileEntityImpSimplyQuarry() {
        super("", 1.4, 3);
    }
    @Override
    public EnumTypeStyle getStyle() {
        return EnumTypeStyle.IMPROVED;
    }
}
