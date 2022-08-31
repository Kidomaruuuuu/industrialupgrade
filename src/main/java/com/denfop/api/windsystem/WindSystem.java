package com.denfop.api.windsystem;

import com.denfop.api.windsystem.event.WindGeneratorEvent;
import com.denfop.tiles.mechanism.wind.TileEntityWindGenerator;
import ic2.core.IC2;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WindSystem implements IWindSystem {

    public static IWindSystem windSystem;
    public double Wind_Strength = 0;
    public EnumTypeWind enumTypeWind = EnumTypeWind.ONE;
    public EnumWindSide windSide;
    public int tick = 12000;
    List<IWindMechanism> mechanismList = new ArrayList<>();
    Random rand;
    Map<EnumFacing, EnumFacing> facingMap = new HashMap<>();
    public int time = 0;
    public WindSystem() {
        windSystem = this;
        MinecraftForge.EVENT_BUS.register(this);
        this.rand = new Random();
        this.windSide = EnumWindSide.getValue(this.rand.nextInt(8));
        facingMap.put(EnumFacing.EAST, EnumFacing.NORTH);
        facingMap.put(EnumFacing.NORTH, EnumFacing.WEST);
        facingMap.put(EnumFacing.WEST, EnumFacing.SOUTH);
        facingMap.put(EnumFacing.SOUTH, EnumFacing.EAST);
    }

    public EnumTypeWind getEnumTypeWind() {
        return enumTypeWind;
    }

    public int getLevelWind(){
        return enumTypeWind.ordinal()+1;
    }
    public EnumWindSide getWindSide() {
        return windSide;
    }

    public void getNewFacing(EnumFacing facing, IWindMechanism windMechanism) {
        if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
            return;
        }
        facing = facingMap.get(facing);
        ((TileEntityWindGenerator) windMechanism).setFacingWrench(facing, null);
        this.changeRotorSide(windMechanism, windMechanism.getFacing());
    }

    @SubscribeEvent
    public void LoadWindMechanism(WindGeneratorEvent event) {

        final IWindMechanism windMechanism = event.getWindMechanism();
        if (event.getLoad()) {
            windMechanism.setCoefficient(getCoefficient(windMechanism));
            if (windMechanism.getAuto()) {
                this.getNewPositionOfMechanism(windMechanism);
            }
            if (!mechanismList.contains(windMechanism)) {
                mechanismList.add(windMechanism);
            }
        } else {
            mechanismList.remove(windMechanism);
        }


    }

    public double getWind_Strength() {
        return Wind_Strength;
    }

    public void changeRotorSide(IWindMechanism windMechanism, EnumFacing facing) {
        windMechanism.setRotorSide(getRotorSide(facing));
        windMechanism.setCoefficient(getCoefficient(windMechanism));
    }

    public void getNewPositionOfMechanism(IWindMechanism windMechanism) {
        final EnumFacing newFacing = getNewFacing();
        ((TileEntityWindGenerator) windMechanism).setFacingWrench(newFacing, null);
        this.changeRotorSide(windMechanism, windMechanism.getFacing());
        IC2.network.get(true).updateTileEntityField( ((TileEntityWindGenerator) windMechanism), "facing");
    }

    public EnumFacing getNewFacing() {
        switch (this.windSide) {
            case E:
            case SE:
                return EnumFacing.SOUTH;
            case W:
            case SW:
                return EnumFacing.NORTH;
            case N:
            case NE:
                return EnumFacing.EAST;
            case S:
            case NW:
                return EnumFacing.WEST;
        }
        return null;

    }

    public EnumRotorSide getRotorSide(EnumFacing facing) {
        switch (facing) {
            case EAST:
                return EnumRotorSide.E;
            case WEST:
                return EnumRotorSide.W;
            case NORTH:
                return EnumRotorSide.N;
            case SOUTH:
                return EnumRotorSide.S;
        }
        return null;
    }

    public double getCoefficient(IWindMechanism windMechanism) {
        if (windMechanism == null) {
            return 0;
        }
        final EnumHorizonSide side = this.windSide.getList().get(0);
        final EnumRotorSide side_rotor = windMechanism.getRotorSide();
        final EnumHorizonSide bad_sides = side_rotor.getBad_sides();
        final EnumHorizonSide good_side = side_rotor.getGood_sides();
        final EnumHorizonSide neutral_side = side_rotor.getNeutral_sides();
        for (EnumHorizonSide side2 : side.getEnumWindSide()) {
            for (EnumHorizonSide side1 : good_side.getEnumWindSide()) {
                if (side1 == side2) {
                    return 1;
                }
            }
            for (EnumHorizonSide side1 : neutral_side.getEnumWindSide()) {
                if (side1 == side2) {
                    return 0.75;
                }
            }
            for (EnumHorizonSide side1 : bad_sides.getEnumWindSide()) {
                if (side1 == side2) {
                    return 0.5;
                }
            }
        }
        return 0;
    }

    public double getSpeed() {
        return Util.limit((this.getWind_Strength()) / (EnumTypeWind.TEN.getMax() * 1.5), 0.0D, 2.0D);
    }

    @SubscribeEvent
    public void windTick(TickEvent.WorldTickEvent event) {
        if (event.world.provider.getDimension() != 0) {
            return;
        }

        tick--;
        if (tick == 0) {
            windSide = EnumWindSide.getValue(this.rand.nextInt(8));
            tick = 12000;
            for (IWindMechanism windMechanism : this.mechanismList) {
                windMechanism.setCoefficient(getCoefficient(windMechanism));
                if (windMechanism.getAuto()) {
                    this.getNewPositionOfMechanism(windMechanism);
                }
            }
        }
        World world = event.world;
        if (world.getWorldTime() % 40 == 0) {
            if (!world.isRaining()) {
                if (!world.isThundering()) {
                    if (world.getWorldInfo().getCleanWeatherTime() > 0) {
                        int time = world.getWorldInfo().getCleanWeatherTime();
                        if (time < 11000 && time >= 8000) {
                            this.time = time-8000;
                            this.enumTypeWind = EnumTypeWind.ONE;
                        }
                        if (time < 8000 && time >= 5000) {
                            this.time = time-5000;
                            this.enumTypeWind = EnumTypeWind.TWO;
                        }
                        if (time < 5000 && time >= 2500) {
                            this.time = time-2500;
                            this.enumTypeWind = EnumTypeWind.THREE;
                        }
                        if (time < 2500 && time >= 1) {
                            this.time = time;
                            this.enumTypeWind = EnumTypeWind.FOUR;
                        }
                        double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                        coef *= 10;
                        this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;
                    } else if (world.getWorldInfo().getRainTime() > 0) {
                        int time = world.getWorldInfo().getRainTime();
                        if (time > 150000) {
                            this.enumTypeWind = EnumTypeWind.ONE;
                            this.time = time-150000;
                        }
                       else if (time < 150000 && time >= 100000) {
                            this.enumTypeWind = EnumTypeWind.TWO;
                            this.time = time-100000;
                        }
                        else  if (time < 100000 && time >= 60000) {
                            this.enumTypeWind = EnumTypeWind.THREE;
                            this.time = time-60000;
                        }
                        else  if (time < 60000 && time >= 20000) {
                            this.enumTypeWind = EnumTypeWind.FOUR;
                            this.time = time-20000;
                        }else  if (time < 20000 && time >= 1) {
                            this.time = time;
                            this.enumTypeWind = EnumTypeWind.FIVE;
                        }
                        double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                        coef *= 10;
                        this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

                    } else if (world.getWorldInfo().getThunderTime() > 0) {
                        int time = world.getWorldInfo().getThunderTime();
                        if (time > 150000) {
                            this.enumTypeWind = EnumTypeWind.ONE;
                            this.time = time-100000;
                        }
                        else if (time < 150000 && time >= 100000) {
                            this.enumTypeWind = EnumTypeWind.TWO;
                            this.time = time-100000;
                        }
                        else  if (time < 100000 && time >= 60000) {
                            this.enumTypeWind = EnumTypeWind.THREE;
                            this.time = time-60000;
                        }
                        else  if (time < 60000 && time >= 20000) {
                            this.enumTypeWind = EnumTypeWind.FOUR;
                            this.time = time-20000;
                        }else  if (time < 20000 && time >= 1) {
                            this.enumTypeWind = EnumTypeWind.FIVE;
                            this.time = time;
                        }
                        double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                        coef *= 10;
                        this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;


                    }
                } else {
                    int time = world.getWorldInfo().getThunderTime();
                    if (time > 20000) {
                        this.enumTypeWind = EnumTypeWind.SEVEN;
                        this.time = time-20000;
                    }
                    if (time < 20000 && time >= 12000) {
                        this.enumTypeWind = EnumTypeWind.EIGHT;
                        this.time = time-12000;
                    }
                    if (time < 12000 && time >= 5000) {
                        this.time = time-5000;
                        this.enumTypeWind = EnumTypeWind.NINE;
                    }
                    if (time < 5000 && time >= 1) {
                        this.time = time;
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
                        this.time = time-20000;
                        this.enumTypeWind = EnumTypeWind.FIVE;
                    }
                    if (time < 20000 && time >= 12000) {
                        this.time = time-12000;
                        this.enumTypeWind = EnumTypeWind.SIX;
                    }
                    if (time < 12000 && time >= 5000) {
                        this.time = time-5000;
                        this.enumTypeWind = EnumTypeWind.SEVEN;
                    }
                    if (time < 5000 && time >= 1) {
                        this.time = time;
                        this.enumTypeWind = EnumTypeWind.EIGHT;
                    }
                    double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                    coef *= 10;
                    this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

                } else if (world.getWorldInfo().getThunderTime() > 0) {
                    int time = world.getWorldInfo().getThunderTime();
                    if (time > 20000) {
                        this.time = time-20000;
                        this.enumTypeWind = EnumTypeWind.SEVEN;
                    }
                    if (time < 20000 && time >= 12000) {
                        this.time = time-12000;
                        this.enumTypeWind = EnumTypeWind.EIGHT;
                    }
                    if (time < 12000 && time >= 5000) {
                        this.time = time-5000;
                        this.enumTypeWind = EnumTypeWind.NINE;
                    }
                    if (time < 5000 && time >= 1) {
                        this.time = time;
                        this.enumTypeWind = EnumTypeWind.TEN;
                    }
                    double coef = this.enumTypeWind.getMax() - this.enumTypeWind.getMin();
                    coef *= 10;
                    this.Wind_Strength = this.enumTypeWind.getMin() + world.rand.nextInt((int) coef + 1) / 10D;

                }
            }
            final double speed = getSpeed();
            for (IWindMechanism windMechanism : this.mechanismList) {
                windMechanism.setRotationSpeed((float) speed);
            }
        }
    }

    public int getTime() {
        return time;
    }

    @Override
    public double getPower(final World world, final BlockPos pos, boolean min) {
        if (world.provider.getDimension() != 0) {
            return 0;
        }
        if (world.isRemote) {
            return 0;
        }

        double coef = this.Wind_Strength;
        int y = pos.getY();
        if (min) {
            y = 150;
        }
        if (y < 150) {
            coef = coef * (y / 150D);
        } else {
            coef = coef * (150D / y);
        }
        return coef * 27;
    }

    @Override
    public double getPowerFromWindRotor(
            final World world,
            final BlockPos pos,
            final IWindMechanism windMechanism,
            ItemStack stack
    ) {

        return this.getPower(world, pos, windMechanism.getMin()) * (windMechanism
                .getRotor()
                .getEfficiency(stack) * (1 + windMechanism.getAdditionalPower())) * (windMechanism.getCoefficient() * (1 + windMechanism.getAdditionalCoefficient()));
    }


}
