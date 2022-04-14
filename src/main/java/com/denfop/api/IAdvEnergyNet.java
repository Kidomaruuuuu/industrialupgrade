package com.denfop.api;

import ic2.api.energy.IEnergyNet;
import ic2.api.energy.tile.IEnergyTile;

public interface IAdvEnergyNet extends IEnergyNet {

    /**
     * @deprecated
     */
    @Deprecated
    double getTotalEnergyEmitted(IEnergyTile var1);

    /**
     * @deprecated
     */
    @Deprecated
    double getTotalEnergySunken(IEnergyTile var1);


    double getPowerFromTier(int var1);

    int getTierFromPower(double var1);


    double getRFFromEU(int amount);


}
