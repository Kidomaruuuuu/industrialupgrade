package com.denfop.tiles.mechanism.generator.energy;

import com.denfop.componets.EnumTypeStyle;

public class TileEntityGeneratorImp extends TileEntityAdvGenerator {

    public TileEntityGeneratorImp() {
        super(3.4, 16000, "iu.blockImpgenerator.name");
    }

    @Override
    public EnumTypeStyle getStyle() {
        return EnumTypeStyle.IMPROVED;
    }

}
