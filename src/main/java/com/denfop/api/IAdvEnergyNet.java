package com.denfop.api;

import aroma1997.uncomplication.enet.SunCoef;
import ic2.api.energy.IEnergyNet;
import net.minecraft.world.World;

public interface IAdvEnergyNet extends IEnergyNet {


    double getPowerFromTier(int var1);

    int getTierFromPower(double var1);


    double getRFFromEU(int amount);

    SunCoef getSunCoefficient(World world);

}
