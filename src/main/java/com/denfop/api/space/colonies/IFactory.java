package com.denfop.api.space.colonies;

public interface IFactory extends IColonieBuilding{

    int getWorkers();

    int getEnergy();

    EnumTypeFactory getType();
}
