package com.denfop.api.windsystem;

import ic2.api.item.IKineticRotor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class WindSystem implements IWindSystem {

    public static IWindSystem windSystem;
    public double Wind_Strength = 0;
    public EnumTypeWind enumTypeWind = EnumTypeWind.ONE;
    public EnumWindSide windSide;
    public int tick = 12000;
    Random rand;
    public WindSystem() {
        windSystem = this;
        MinecraftForge.EVENT_BUS.register(this);
        this.rand = new Random();
        this.windSide = EnumWindSide.getValue(this.rand.nextInt(8));
    }

    @SubscribeEvent
    public void windTick(TickEvent.WorldTickEvent event) {
        if (event.world.provider.getDimension() != 0) {
            return;
        }
        if(event.world.isRemote)
            return;
        tick--;
        if(tick == 0){
            windSide = EnumWindSide.getValue(this.rand.nextInt(8));
            tick = 12000;

        }

        World world = event.world;
        if (!world.isRaining()) {
            if (!world.isThundering()) {
                if (world.getWorldInfo().getCleanWeatherTime() > 0) {
                    int time = world.getWorldInfo().getCleanWeatherTime();
                    if (time < 11000 && time >= 8000) {
                        this.enumTypeWind = EnumTypeWind.ONE;
                    }
                    if (time < 8000 && time >= 5000) {
                        this.enumTypeWind = EnumTypeWind.TWO;
                    }
                    if (time < 5000 && time >= 2500) {
                        this.enumTypeWind = EnumTypeWind.THREE;
                    }
                    if (time < 2500 && time >= 1) {
                        this.enumTypeWind = EnumTypeWind.FOUR;
                    }
                    double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                    coef *= 10;
                    this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;
                } else if (world.getWorldInfo().getRainTime() > 0) {
                    int time = world.getWorldInfo().getRainTime();
                    if (time > 100000) {
                        this.enumTypeWind = EnumTypeWind.ONE;
                    }
                    if (time < 100000 && time >= 65000) {
                        this.enumTypeWind = EnumTypeWind.TWO;
                    }
                    if (time < 65000 && time >= 20000) {
                        this.enumTypeWind = EnumTypeWind.THREE;
                    }
                    if (time < 20000 && time >= 1) {
                        this.enumTypeWind = EnumTypeWind.FOUR;
                    }
                    double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                    coef *= 10;
                    this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

                } else if (world.getWorldInfo().getThunderTime() > 0) {
                    int time = world.getWorldInfo().getThunderTime();
                    if (time > 100000) {
                        this.enumTypeWind = EnumTypeWind.ONE;
                    }
                    if (time < 100000 && time >= 65000) {
                        this.enumTypeWind = EnumTypeWind.TWO;
                    }
                    if (time < 65000 && time >= 20000) {
                        this.enumTypeWind = EnumTypeWind.THREE;
                    }
                    if (time < 20000 && time >= 1) {
                        this.enumTypeWind = EnumTypeWind.FOUR;
                    }
                    double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                    coef *= 10;
                    this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;


                }
            } else {
                int time = world.getWorldInfo().getThunderTime();
                if (time > 20000) {
                    this.enumTypeWind = EnumTypeWind.SEVEN;
                }
                if (time < 20000 && time >= 12000) {
                    this.enumTypeWind = EnumTypeWind.EIGHT;
                }
                if (time < 12000 && time >= 5000) {
                    this.enumTypeWind = EnumTypeWind.NINE;
                }
                if (time < 5000 && time >= 1) {
                    this.enumTypeWind = EnumTypeWind.TEN;
                }
                double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                coef *= 10;
                this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

            }
        } else {

            if (world.getWorldInfo().getRainTime() > 0 && world.isRaining() && !world.isThundering()) {
                int time = world.getWorldInfo().getRainTime();
                if (time > 20000) {
                    this.enumTypeWind = EnumTypeWind.FOUR;
                }
                if (time < 20000 && time >= 12000) {
                    this.enumTypeWind = EnumTypeWind.FIVE;
                }
                if (time < 12000 && time >= 5000) {
                    this.enumTypeWind = EnumTypeWind.SIX;
                }
                if (time < 5000 && time >= 1) {
                    this.enumTypeWind = EnumTypeWind.SEVEN;
                }
                double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                coef *= 10;
                this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

            } else if (world.getWorldInfo().getThunderTime() > 0) {
                int time = world.getWorldInfo().getThunderTime();
                if (time > 20000) {
                    this.enumTypeWind = EnumTypeWind.SEVEN;
                }
                if (time < 20000 && time >= 12000) {
                    this.enumTypeWind = EnumTypeWind.EIGHT;
                }
                if (time < 12000 && time >= 5000) {
                    this.enumTypeWind = EnumTypeWind.NINE;
                }
                if (time < 5000 && time >= 1) {
                    this.enumTypeWind = EnumTypeWind.TEN;
                }
                double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                coef *= 10;
                this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

            }
        }
    }

    @Override
    public double getPower(final World world, final BlockPos pos) {
        if (world.provider.getDimension() != 0) {
            return 0;
        }
        double coef = this.Wind_Strength;
        if (pos.getY() < 150) {
            coef = coef * (pos.getY() / 150D);
        } else {
            coef = coef * (150D / pos.getY());
        }
        return coef * 25;
    }

    @Override
    public double getPowerFromWindRotor(final World world, final BlockPos pos, final IKineticRotor rotor, ItemStack stack) {
        return this.getPower(world, pos) * rotor.getEfficiency(stack);
    }


}
