package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public abstract class Building implements IColonieBuilding {

    private final String name;
    private final IColonie colonie;

    public Building(String name,IColonie colonie) {
        this.name = name;
        this.colonie = colonie;
        this.getColonie().addBuilding(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
        tag.setString("name", this.name);
        return tag;
    }

    @Override
    public IColonie getColonie() {
        return this.colonie;
    }

    @Override
    public void work() {

    }


}
