package com.denfop.heat;


import com.denfop.api.heat.IHeatNet;
import com.denfop.api.heat.IHeatTile;
import com.denfop.api.heat.NodeHeatStats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public class HeatNetGlobal implements IHeatNet {

    private static Map<World, HeatNetLocal> worldToEnergyNetMap;

    static {
        HeatNetGlobal.worldToEnergyNetMap = new WeakHashMap<>();
    }

    public static HeatNetGlobal initialize() {
        new EventHandler();
        return new HeatNetGlobal();
    }

    public static void onWorldUnload(World world) {
        final HeatNetLocal local = HeatNetGlobal.worldToEnergyNetMap.remove(world);
        if (local != null) {
            local.onUnload();
        }
    }

    public static HeatNetLocal getForWorld(final World world) {
        if (world == null) {
            return null;
        }
        if (!HeatNetGlobal.worldToEnergyNetMap.containsKey(world)) {
            HeatNetGlobal.worldToEnergyNetMap.put(world, new HeatNetLocal(world));
        }
        return HeatNetGlobal.worldToEnergyNetMap.get(world);
    }

    public static void onTickStart(final World world) {
        final HeatNetLocal energyNet = getForWorld(world);
        if (energyNet != null) {
            energyNet.onTickStart();
        }
    }

    public static void onTickEnd(final World world) {
        final HeatNetLocal energyNet = getForWorld(world);
        if (energyNet != null) {
            energyNet.onTickEnd();
        }
    }

    @Override
    public IHeatTile getTile(final World var1, final BlockPos var2) {
        if (var1.getTileEntity(var2) instanceof IHeatTile) {
            return (IHeatTile) var1.getTileEntity(var2);
        }
        return null;
    }

    @Override
    public IHeatTile getSubTile(final World var1, final BlockPos var2) {
        return getTile(var1, var2);
    }

    @Override
    public <T extends TileEntity & IHeatTile> void addTile(final T var1) {
        final HeatNetLocal local = getForWorld(var1.getWorld());
        local.addTile(var1);
    }

    @Override
    public void removeTile(final IHeatTile var1) {
        final HeatNetLocal local = getForWorld(var1.getWorldTile());
        local.removeTile(var1);
    }

    @Override
    public NodeHeatStats getNodeStats(final IHeatTile var1) {
        final HeatNetLocal local = getForWorld(var1.getWorldTile());
        if (local == null) {
            return new NodeHeatStats(0.0, 0.0);
        }
        return local.getNodeStats(var1);
    }


}
