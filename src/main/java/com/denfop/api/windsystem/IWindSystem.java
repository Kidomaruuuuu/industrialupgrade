package com.denfop.api.windsystem;

import ic2.api.item.IKineticRotor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWindSystem {

    double getPower(World world, BlockPos pos);

    double getPowerFromWindRotor(World world, BlockPos pos, IKineticRotor rotor, ItemStack stack);

}
