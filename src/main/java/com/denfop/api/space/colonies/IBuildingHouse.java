package com.denfop.api.space.colonies;

public interface IBuildingHouse extends IColonieBuilding {

    EnumHousesLevel getLevel();

    int getPeople();

    void setPeoples(int peoples);

    int getEnergy();

    int getMaxPeople();

    void addPeople(int peoples);

    int getWorkers();

}
