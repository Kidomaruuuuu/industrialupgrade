package com.denfop.api.space.colonies;

public interface IColoniePanelFactory extends IColonieBuilding {
    int getGeneration();

    int getPeople();

    EnumTypeSolarPanel getType();
}
