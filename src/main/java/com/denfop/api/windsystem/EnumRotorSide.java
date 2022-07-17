package com.denfop.api.windsystem;

public enum EnumRotorSide {
    W(EnumHorizonSide.SN, EnumHorizonSide.W, EnumHorizonSide.E),
    E(EnumHorizonSide.SN, EnumHorizonSide.E, EnumHorizonSide.W),
    N(EnumHorizonSide.EW, EnumHorizonSide.N, EnumHorizonSide.S),
    S(EnumHorizonSide.EW, EnumHorizonSide.S, EnumHorizonSide.N);

    public final EnumHorizonSide good_sides;
    public final EnumHorizonSide neutral_sides;
    public final EnumHorizonSide bad_sides;

    EnumRotorSide(EnumHorizonSide good_sides, EnumHorizonSide neutral_sides, EnumHorizonSide bad_sides){
        this.good_sides = good_sides;
        this.neutral_sides = neutral_sides;
        this.bad_sides = bad_sides;
    };



}
