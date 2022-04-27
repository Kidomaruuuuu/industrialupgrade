package com.denfop.api.space.colonies;

import net.minecraft.nbt.NBTTagCompound;

public class OxygenFactory extends Building implements  IOxygenFactory{

    private final int max;
    private final int generation;
    private final int people;
    private final int energy;

    public OxygenFactory(final String name, final IColonie colonie) {
        super(name, colonie);
        this.max = 5000;
        this.generation = 80;
        this.people = 0;
        this.energy = 10;
        this.getColonie().addNeededWorkers(this.getPeople());
        this.getColonie().addConsumeEnergy(this.getEnergy());
        this.getColonie().addMaxOxygen(this.getMax());
        this.getColonie().getOxygenFactory().add(this);
    }
    public OxygenFactory(final String name, final IColonie colonie, int people) {
        super(name, colonie);
        this.max = 5000;
        this.generation = 60;
        this.people = people;
        this.energy = 10;
        this.getColonie().addNeededWorkers(this.getPeople());
        this.getColonie().addConsumeEnergy(this.getEnergy());
        this.getColonie().addMaxOxygen(this.getMax());
        this.getColonie().getOxygenFactory().add(this);
    }
    public OxygenFactory(NBTTagCompound tag, final IColonie colonie) {
        super(tag.getString("name"), colonie);
        this.max = tag.getInteger("max");
        this.generation = tag.getInteger("generation");
        this.people = tag.getInteger("people");
        this.energy = tag.getInteger("energy");
        this.getColonie().addNeededWorkers(this.getPeople());
        this.getColonie().addConsumeEnergy(this.getEnergy());
        this.getColonie().addMaxOxygen(this.getMax());
        this.getColonie().getOxygenFactory().add(this);
    }
    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public int getGeneration() {
        return this.generation;
    }

    @Override
    public int getEnergy() {
        return this.energy;
    }

    @Override
    public boolean needWorkers() {
        return this.getPeople() > 0;
    }

    @Override
    public int getPeople() {
        return this.people;
    }

    @Override
    public void work() {
        if(this.getColonie().getOxygen() < this.getColonie().getMaxOxygen())
        if(this.getColonie().getEnergy() > this.getEnergy()){
            int temp = Math.min(this.getGeneration(), this.getColonie().getMaxOxygen() - this.getColonie().getEnergy());
            this.getColonie().addOxygen(temp);
        }else{
            if (!this.getColonie().getProblems().contains(EnumProblems.ENERGY)) {
                this.getColonie().getProblems().add(EnumProblems.ENERGY);
            }
        }
    }

    @Override
    public NBTTagCompound writeTag(final NBTTagCompound tag) {
        super.writeTag(tag);
        tag.setInteger("max",this.max);
        tag.setInteger("people",this.people);
        tag.setInteger("energy",this.energy);
        tag.setInteger("generation",this.generation);
        tag.setString("type","oxygenfactory");
        return tag;
    }

    @Override
    public void remove() {
        this.getColonie().removeNeededWorkers(this.getPeople());
        this.getColonie().removeConsumeEnergy(this.getEnergy());
        this.getColonie().decreaseOxygen(this.getMax());
        this.getColonie().getOxygenFactory().remove(this);
    }

}
