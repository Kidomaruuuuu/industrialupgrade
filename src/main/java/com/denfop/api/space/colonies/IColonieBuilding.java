package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public interface IColonieBuilding {

    String getName();

    NBTTagCompound writeTag(NBTTagCompound tag);

    IColonie getColonie();

    void work();

    void remove();

}
