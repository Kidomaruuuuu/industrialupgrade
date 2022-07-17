package com.denfop.tiles.base;

public enum EnumTypeCollector {
    END(3),
    NETHER(5);

    private final int meta;

    EnumTypeCollector(int meta) {
        this.meta = meta;
    }

    public int getMeta() {
        return meta;
    }
}
