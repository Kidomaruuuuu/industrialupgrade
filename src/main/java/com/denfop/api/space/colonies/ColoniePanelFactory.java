package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public class ColoniePanelFactory extends Building implements IColoniePanelFactory{

    private final EnumTypeSolarPanel type;

    public ColoniePanelFactory(final String name, final IColonie colonie, EnumTypeSolarPanel type) {
        super(name, colonie);
        this.type= type;
        this.getColonie().addNeededWorkers(this.type.getPeople());
        this.getColonie().addMaxEnergy(5000);
    }
    public ColoniePanelFactory(final NBTTagCompound tag,final IColonie colonie ){
        super(tag.getString("name"), colonie);
        this.type= EnumTypeSolarPanel.getID(tag.getInteger("id"));
        this.getColonie().addNeededWorkers(this.type.getPeople());
        this.getColonie().addMaxEnergy(5000);
    }
    @Override
    public int getGeneration() {
        return this.type.getGeneration();
    }

    @Override
    public int getPeople() {
        return this.type.getPeople();
    }

    @Override
    public EnumTypeSolarPanel getType() {
        return this.type;
    }

    @Override
    public void work() {
        if(this.getColonie().getEnergy() < this.getColonie().getMaxEnergy()){
            int temp = Math.min(this.getGeneration(), this.getColonie().getMaxEnergy() - this.getColonie().getEnergy());
            this.getColonie().addEnergy(temp);
        }
    }

    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
        super.writeTag(tag);
        tag.setString("type","solar");
        tag.setInteger("id",this.getType().ordinal());
        return tag;
    }

    @Override
    public void remove() {
        this.getColonie().removeNeededWorkers(this.type.getPeople());
        this.getColonie().decreaseEnergy(5000);
    }

}
