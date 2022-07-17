package aroma1997.uncomplication.enet.old;


import aroma1997.uncomplication.enet.SunCoef;
import com.denfop.Config;
import com.denfop.api.IAdvEnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyNetGlobal implements IAdvEnergyNet {

    private static Map<Integer, EnergyNetLocal> worldToEnergyNetMap;

    static {
        EnergyNetGlobal.worldToEnergyNetMap = new WeakHashMap<>();
    }

    public static EnergyNetLocal getForWorld(final World world) {
        if (world == null) {
            return null;
        }
        final int id = world.provider.getDimension();
        if (!worldToEnergyNetMap.containsKey(id)) {
           worldToEnergyNetMap.put(id, new EnergyNetLocal(world));
        }
        return worldToEnergyNetMap.get(id);
    }


    public static void onTickEnd(final World world) {
        final EnergyNetLocal energyNet = getForWorld(world);
        if (energyNet != null) {
            energyNet.onTickEnd();
        }
    }

    public static EnergyNetGlobal initialize() {
        MinecraftForge.EVENT_BUS.unregister(ic2.core.energy.grid.EventHandler.class);
        new EventHandler();
        return new EnergyNetGlobal();
    }

    public static void onWorldUnload(final World world) {
        final EnergyNetLocal local = EnergyNetGlobal.worldToEnergyNetMap.remove(world.provider.getDimension());
        if (local != null) {
            local.onUnload();
        }
    }

    public IEnergyTile getTileEntity(final World world, final int x, final int y, final int z) {
        final EnergyNetLocal local = getForWorld(world);
        if (local != null) {
            return local.getTileEntity(new BlockPos(x, y, z));
        }
        return null;
    }

    public IEnergyTile getTileEntity(final World world, BlockPos pos) {
        final EnergyNetLocal local = getForWorld(world);
        if (local != null) {
            return local.getTileEntity(pos);
        }
        return null;
    }


    @Override
    public IEnergyTile getTile(final World world, final BlockPos blockPos) {
        final EnergyNetLocal local = getForWorld(world);
        if (local != null) {
            return local.getTileEntity(blockPos);
        }
        return null;
    }

    @Override
    public IEnergyTile getSubTile(final World world, final BlockPos blockPos) {


        return this.getTileEntity(world, blockPos);
    }

    @Override
    public <T extends TileEntity & IEnergyTile> void addTile(final T t) {

    }

    @Override
    public <T extends ILocatable & IEnergyTile> void addTile(final T t) {

    }

    @Override
    public void removeTile(final IEnergyTile iEnergyTile) {

    }

    @Override
    public World getWorld(final IEnergyTile tile) {
        if (tile == null) {
            return null;
        } else if (tile instanceof ILocatable) {
            return ((ILocatable) tile).getWorldObj();
        } else if (tile instanceof TileEntity) {
            return ((TileEntity) tile).getWorld();
        } else {
            throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
        }
    }

    @Override
    public BlockPos getPos(final IEnergyTile iEnergyTile) {
        final EnergyNetLocal local = getForWorld(this.getWorld(iEnergyTile));
        if (local != null) {
            return local.getPos(iEnergyTile);
        }
        return null;
    }

    @Override
    public NodeStats getNodeStats(final IEnergyTile te) {
        final EnergyNetLocal local = getForWorld(getWorld(te));
        if (local == null) {
            return new NodeStats(0.0, 0.0, 0.0);
        }
        return local.getNodeStats(te);
    }

    @Override
    public boolean dumpDebugInfo(
            final World world,
            final BlockPos blockPos,
            final PrintStream printStream,
            final PrintStream printStream1
    ) {
        return false;
    }

    public double getPowerFromTier(final int tier) {


        return tier < 22 ? 8.0D * Math.pow(4.0D, tier) : 9.223372036854776E18D;

    }

    public int getTierFromPower(final double power) {
        if (power <= 0.0) {
            return 0;
        }
        return (int) Math.ceil(Math.log(power / 8.0) / Math.log(4.0));
    }

    @Override
    public double getRFFromEU(final int amount) {
        return amount * Config.coefficientrf;
    }

    @Override
    public SunCoef getSunCoefficient(final World world) {
        return  getForWorld(world).getSuncoef();
    }

    public synchronized void registerEventReceiver(IEnergyNetEventReceiver receiver) {

    }

    public synchronized void unregisterEventReceiver(IEnergyNetEventReceiver receiver) {

    }

}
