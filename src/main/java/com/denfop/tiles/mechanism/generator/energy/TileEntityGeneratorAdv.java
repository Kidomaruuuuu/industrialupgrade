package com.denfop.tiles.mechanism.generator.energy;

import com.denfop.componets.EnumTypeStyle;

public class TileEntityGeneratorAdv extends TileEntityAdvGenerator {

    public TileEntityGeneratorAdv() {
        super(2.2, 8000, "iu.blockAdvgenerator.name");
    }

    @Override
    public EnumTypeStyle getStyle() {
        return EnumTypeStyle.ADVANCED;
    }

}
