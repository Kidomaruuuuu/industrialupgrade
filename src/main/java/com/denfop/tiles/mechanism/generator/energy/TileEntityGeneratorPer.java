package com.denfop.tiles.mechanism.generator.energy;

import com.denfop.componets.EnumTypeStyle;

public class TileEntityGeneratorPer extends TileEntityAdvGenerator {

    public TileEntityGeneratorPer() {
        super(4.6, 32000, "iu.blockPergenerator.name");
    }
    @Override
    public EnumTypeStyle getStyle() {
        return EnumTypeStyle.PERFECT;
    }
}
