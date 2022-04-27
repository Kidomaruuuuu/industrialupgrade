package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public class ProtectionBuilding extends Building implements IProtectionBuilding{

    private final int protection;

    public ProtectionBuilding(final String name, final IColonie colonie) {
        super(name, colonie);
        this.protection = 50;
        this.getColonie().addProtection(this.getProtection());
    }
    public ProtectionBuilding(final NBTTagCompound tag, final IColonie colonie) {
        super(tag.getString("name"), colonie);
        this.protection = 50;
        this.getColonie().addProtection(this.getProtection());
    }
    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
        super.writeTag(tag);
        tag.setString("type","protection");
        return tag;
    }

    @Override
    public void remove() {

    }

    @Override
    public int getProtection() {
        return this.protection;
    }

}
