package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public class StorageBuilding extends  Building implements  IColonieStorage{

    boolean work;
    IStorage storage;
    int energy;
    int peoples;
    public StorageBuilding(final String name, final IColonie colonie) {
        super(name, colonie);
        this.storage = new Storage(this);
        this.energy = 10;
        this.peoples = 5;
        this.getColonie().addStorage(this.storage);
        this.getColonie().addNeededWorkers(this.peoples);
        this.getColonie().addConsumeEnergy(this.energy);
        this.work = true;
    }
    public StorageBuilding(final NBTTagCompound tag, final IColonie colonie ){
        super(tag.getString("name"), colonie);
        this.peoples =  tag.getInteger("people");
        this.energy =  tag.getInteger("energy");
        this.work = tag.getBoolean("work");
        this.storage = new Storage(tag.getCompoundTag("storage"), this);
        this.getColonie().addStorage(this.storage);
        this.getColonie().addNeededWorkers(this.peoples);
        this.getColonie().addConsumeEnergy(this.energy);
    }
    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
       super.writeTag(tag);
       tag.setInteger("people",this.peoples);
       tag.setInteger("energy",this.energy);
       tag.setBoolean("work",this.work);
       tag.setString("type","storage");
       tag.setTag("storage",this.getStorage().writeNBT(new NBTTagCompound()));
       return tag;
    }

    @Override
    public IStorage getStorage() {
        return this.storage;
    }

    @Override
    public int getEnergy() {
        return this.energy;
    }

    @Override
    public int getPeoples() {
        return this.peoples;
    }

    @Override
    public boolean getWork() {
        return this.work;
    }

    @Override
    public void setWork(final boolean setWork) {
       this.work = setWork;
    }

    @Override
    public void work() {
        if(this.getColonie().getEnergy() <  this.getEnergy()){
            this.getColonie().useEnergy(this.getEnergy());
            this.getColonie().getProblems().remove(EnumProblems.ENERGY);
            if(!this.getWork())
                this.setWork(true);
        }else{
            if (!this.getColonie().getProblems().contains(EnumProblems.ENERGY)) {
                this.getColonie().getProblems().add(EnumProblems.ENERGY);
            }
            if(this.getWork())
                this.setWork(false);
        }
    }

    @Override
    public void remove() {
        this.getColonie().removeStorage(this.storage);
        this.getColonie().removeNeededWorkers(this.peoples);
        this.getColonie().removeConsumeEnergy(10);
    }

}
