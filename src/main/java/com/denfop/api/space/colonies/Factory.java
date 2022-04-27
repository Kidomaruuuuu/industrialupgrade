package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public class Factory extends Building implements IFactory{

    public final EnumTypeFactory typeFactory;

    public Factory(final String name, final IColonie colonie, EnumTypeFactory typeFactory) {
        super(name, colonie);
        this.typeFactory = typeFactory;
        this.getColonie().addWorkers(this.getWorkers());
        this.getColonie().addConsumeEnergy(this.getEnergy());
    }
    public Factory(final NBTTagCompound tag, IColonie colonie) {
        super(tag.getString("name"), colonie);
        int id = tag.getInteger("id");
        this.typeFactory = EnumTypeFactory.getID(id);
        this.getColonie().addWorkers(this.getWorkers());
        this.getColonie().addConsumeEnergy(this.getEnergy());
    }
    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
        super.writeTag(tag);
        tag.setString("type","factory");
        tag.setInteger("id", this.getType().ordinal());
        return tag;
    }
    @Override
    public void remove() {

    }

    @Override
    public int getWorkers() {
        return this.getType().getPeople();
    }

    @Override
    public int getEnergy() {
        return this.getType().getEnergy();
    }

    @Override
    public EnumTypeFactory getType() {
        return this.typeFactory;
    }

}
