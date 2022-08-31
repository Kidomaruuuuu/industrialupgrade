package com.denfop.api.windsystem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWindSystem {

    double getPower(World world, BlockPos pos, boolean min);

    double getPowerFromWindRotor(World world, BlockPos pos, IWindMechanism rotor, ItemStack stack);

    EnumRotorSide getRotorSide(EnumFacing facing);

    void changeRotorSide(IWindMechanism windMechanism, EnumFacing facing);

    EnumFacing getNewFacing();

    void getNewPositionOfMechanism(IWindMechanism windMechanism);

    double getWind_Strength();

    double getSpeed();

    EnumWindSide getWindSide();

    void getNewFacing(EnumFacing facing, IWindMechanism windMechanism);

    int getTime();

    int getLevelWind();

    EnumTypeWind getEnumTypeWind();
}
