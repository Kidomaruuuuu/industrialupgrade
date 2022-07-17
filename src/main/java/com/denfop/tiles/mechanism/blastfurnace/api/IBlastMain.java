package com.denfop.tiles.mechanism.blastfurnace.api;

public interface IBlastMain  {

    boolean getFull();

    void update_block();

    IBlastHeat getHeat();

    IBlastInputFluid getInputFluid();

    IBlastInputItem getInputItem();

    IBlastOutputItem getOutputItem();

    void setHeat(IBlastHeat blastHeat);

    void setInputFluid(IBlastInputFluid blastInputFluid);

    void setInputItem(IBlastInputItem blastInputItem);

    void setOutputItem(IBlastOutputItem blastOutputItem);

    void setFull(boolean full);

    double getProgress();
}
