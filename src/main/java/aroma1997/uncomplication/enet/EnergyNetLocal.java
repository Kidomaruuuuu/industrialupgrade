package aroma1997.uncomplication.enet;

import com.denfop.Config;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.info.ILocatable;
import ic2.core.ExplosionIC2;
import ic2.core.block.wiring.TileEntityTransformer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnergyNetLocal {

    private final World world;
    private final EnergyPathMap energySourceToEnergyPathMap;
    private final Map<IEnergyTile, BlockPos> chunkCoordinatesMap;
    private final Map<IEnergyTile, TileEntity> energyTileTileEntityMap;
    private final Map<BlockPos, IEnergyTile> chunkCoordinatesIEnergyTileMap;
    private final List<IEnergySource> sources;
    private final EnergyNetLocal.WaitingList waitingList;

    EnergyNetLocal(World world) {
        this.energySourceToEnergyPathMap = new EnergyNetLocal.EnergyPathMap();
        this.sources = new ArrayList<>();
        this.waitingList = new WaitingList();
        this.world = world;
        this.chunkCoordinatesIEnergyTileMap = new HashMap<>();
        this.chunkCoordinatesMap = new HashMap<>();
        this.energyTileTileEntityMap = new HashMap<>();
    }

    public void addTile(IEnergyTile tile1) {
        if (tile1 instanceof IMetaDelegate) {
            final List<IEnergyTile> tiles = ((IMetaDelegate) tile1).getSubTiles();
            for (final IEnergyTile tile : tiles) {
                this.addTileEntity(getTileFromIEnergy(tile).getPos(), tile1, tile);
            }
            if (tile1 instanceof IEnergySource) {
                this.sources.add((IEnergySource) tile1);
            }


        } else {
            this.addTileEntity(getTileFromIEnergy(tile1).getPos(), tile1);

        }
    }

    public void addTileEntity(final BlockPos coords, final IEnergyTile tile) {
        if (this.chunkCoordinatesIEnergyTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromIEnergy(tile);
        this.energyTileTileEntityMap.put(tile, te);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesIEnergyTileMap.put(coords, tile);
        this.update(coords.getX(), coords.getY(), coords.getZ());
        if (tile instanceof IEnergyAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), tile);
        }
        if (tile instanceof IEnergySource && !(tile instanceof IMetaDelegate)) {
            this.sources.add((IEnergySource) tile);
        }
    }

    public void addTileEntity(final BlockPos coords, final IEnergyTile tile, final IEnergyTile tile1) {
        if (this.chunkCoordinatesIEnergyTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromIEnergy(tile);
        TileEntity te1 = getTileFromIEnergy(tile1);
        this.energyTileTileEntityMap.put(tile, te);
        this.energyTileTileEntityMap.put(tile1, te1);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesIEnergyTileMap.put(coords, tile);

        this.update(coords.getX(), coords.getY(), coords.getZ());
        if (tile instanceof IEnergyAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), tile);
        }
        if (tile instanceof IEnergySource && !(tile instanceof IMetaDelegate)) {
            this.sources.add((IEnergySource) tile);

        }


    }

    public void removeTile(IEnergyTile tile1) {
        if (tile1 instanceof IMetaDelegate) {
            final List<IEnergyTile> tiles = ((IMetaDelegate) tile1).getSubTiles();
            for (final IEnergyTile tile : tiles) {
                BlockPos coord1;
                if (this.energyTileTileEntityMap.containsKey(tile)) {
                    coord1 = this.energyTileTileEntityMap.get(tile).getPos();
                } else {
                    coord1 = getTileFromIEnergy(tile).getPos();
                }
                this.removeTileEntity(coord1, tile1, tile);

            }
        } else {
            this.removeTileEntity(tile1);
        }
    }

    public void removeTileEntity(BlockPos coord, IEnergyTile tile, IEnergyTile tile1) {
        if (!this.chunkCoordinatesIEnergyTileMap.containsKey(coord)) {
            return;
        }
        this.chunkCoordinatesMap.remove(tile, coord);
        this.chunkCoordinatesIEnergyTileMap.remove(coord);
        this.energyTileTileEntityMap.remove(tile1, this.energyTileTileEntityMap.get(tile1));
        this.energyTileTileEntityMap.remove(tile, this.energyTileTileEntityMap.get(tile));
        this.update(coord.getX(), coord.getY(), coord.getZ());
        if (tile instanceof IEnergyAcceptor) {
            this.energySourceToEnergyPathMap.removeAll(this.energySourceToEnergyPathMap.getSources((IEnergyAcceptor) tile));
            this.waitingList.onTileEntityRemoved(tile);
        }
        if (tile instanceof IEnergySource) {
            this.sources.remove((IEnergySource) tile);
            this.energySourceToEnergyPathMap.remove((IEnergySource) tile);
        }
    }

    public void removeTileEntity(IEnergyTile tile) {
        if (!this.energyTileTileEntityMap.containsKey(tile)) {
            return;
        }
        final BlockPos coord = this.chunkCoordinatesMap.get(tile);
        this.chunkCoordinatesMap.remove(tile);
        this.energyTileTileEntityMap.remove(tile, this.energyTileTileEntityMap.get(tile));
        this.chunkCoordinatesIEnergyTileMap.remove(coord, tile);
        this.update(coord.getX(), coord.getY(), coord.getZ());
        if (tile instanceof IEnergyAcceptor) {
            this.energySourceToEnergyPathMap.removeAll(this.energySourceToEnergyPathMap.getSources((IEnergyAcceptor) tile));
            this.waitingList.onTileEntityRemoved(tile);
        }
        if (tile instanceof IEnergySource) {
            this.sources.remove((IEnergySource) tile);
            this.energySourceToEnergyPathMap.remove((IEnergySource) tile);
        }
    }

    public double emitEnergyFrom(IEnergySource energySource, double amount) {
        double source = energySource.getSourceTier();
        List<EnergyPath> energyPaths = this.energySourceToEnergyPathMap.get(energySource);
        if(energyPaths == null) {
            this.energySourceToEnergyPathMap.put(energySource, this.discover(energySource));
            energyPaths = this.energySourceToEnergyPathMap.get(energySource);
        }
        if(amount > 0.0D)
        for (EnergyPath energyPath : energyPaths) {
            if(amount <= 0.0D)
                break;
            IEnergySink energySink = energyPath.target;
            if (energySink.getDemandedEnergy() <= 0.0D) {
                continue;
            }
            if (energyPath.loss >= amount) {
                continue;
            }
            if (Config.enableIC2EasyMode && conductorToWeak(energyPath.conductors, amount)) {
                continue;
            }
            double energyConsumed = 0.0D;
            double energyProvided = Math.floor(Math.round(amount));
                double energyLoss = Math.floor(energyPath.loss);
                if (energyProvided > energyLoss) {
                    double providing = energyProvided - energyLoss;
                    double adding = Math.min(providing, energySink.getDemandedEnergy());
                    if (adding <= 0.0D) {
                        continue;
                    }
                    int tier = energySink.getSinkTier();
                    int tier1 = EnergyNet.instance.getTierFromPower(providing);
                    if (tier1 > tier && !Config.enableIC2EasyMode) {
                        explodeTiles(energySink);
                        continue;
                    }
                    double energyReturned = energySink.injectEnergy(energyPath.targetDirection, adding, source);
                    if (energyReturned >= energyProvided - energyLoss) {
                        energyReturned = energyProvided - energyLoss;
                    }
                    energyConsumed += adding;
                    energyConsumed -= energyReturned;

                    double energyInjected = adding - energyConsumed;
                    amount -= energyConsumed;
                    amount = Math.max(0, amount);
                    energyPath.totalEnergyConducted = (long) energyInjected;
                    energyPath.maxSendedEnergy = (long) Math.max(energyPath.maxSendedEnergy, energyInjected);
                    for (IEnergyConductor energyConductor3 : energyPath.conductors) {
                        if (energyInjected > energyPath.minInsulationEnergyAbsorption &&
                                energyInjected >= energyPath.minInsulationBreakdownEnergy){
                            if (energyInjected >= energyConductor3.getInsulationBreakdownEnergy()) {
                                energyConductor3.removeInsulation();
                                if (energyConductor3.getInsulationEnergyAbsorption() >= energyPath.minInsulationEnergyAbsorption) {
                                    continue;
                                }
                                energyPath.minInsulationEnergyAbsorption =
                                        (int) energyConductor3.getInsulationEnergyAbsorption();
                            }
                        }

                        if (energyInjected >= energyConductor3.getConductorBreakdownEnergy()) {
                            energyConductor3.removeConductor();
                        }

                    }
                }


        }

        return amount;
    }

    public double getTotalEnergyEmitted(IEnergyTile tileEntity) {
        double ret = 0.0D;
        if (tileEntity instanceof IEnergyConductor) {
            for (EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity)) {
                if (energyPath.conductors.contains(tileEntity)) {
                    ret += energyPath.totalEnergyConducted;
                }
            }
        }
        if (tileEntity instanceof IEnergySource && this.energySourceToEnergyPathMap.containsKey((IEnergySource) tileEntity)) {
            for (EnergyPath energyPath2 : this.energySourceToEnergyPathMap.get((IEnergySource) tileEntity)) {
                ret += energyPath2.totalEnergyConducted;
            }
        }
        return ret;
    }

    public double getTotalEnergySunken(IEnergyTile tileEntity) {
        double ret = 0.0D;
        if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink) {
            for (EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity)) {
                if ((tileEntity instanceof IEnergySink && energyPath.target == tileEntity) || (tileEntity instanceof IEnergyConductor && energyPath.conductors.contains(
                        tileEntity))) {
                    ret += energyPath.totalEnergyConducted;
                }
            }
        }
        return ret;
    }

    public TileEntity getTileFromMap(IEnergyTile tile) {
        return this.energyTileTileEntityMap.get(tile);
    }

    public TileEntity getTileFromIEnergy(IEnergyTile tile) {
        if (tile instanceof TileEntity) {
            return (TileEntity) tile;
        }
        if (tile instanceof ILocatable) {
            return this.world.getTileEntity(((ILocatable) tile).getPosition());
        }
        return null;
    }

    private List<EnergyPath> discover(IEnergyTile emitter) {
        Map<IEnergyTile, EnergyBlockLink> reachedTileEntities = new HashMap<>();
        LinkedList<IEnergyTile> tileEntitiesToCheck = new LinkedList<>();
        tileEntitiesToCheck.add(emitter);
        while (!tileEntitiesToCheck.isEmpty()) {
            IEnergyTile currentTileEntity = tileEntitiesToCheck.remove();
            TileEntity tile = getTileFromMap(emitter);
            if(tile == null){
                tile = this.getTileFromIEnergy(currentTileEntity);
            }
            if (!tile.isInvalid()) {
                List<EnergyTarget> validReceivers = getValidReceivers(currentTileEntity, false);
                for (EnergyTarget validReceiver : validReceivers) {
                    if (validReceiver.tileEntity != emitter) {
                        if (reachedTileEntities.containsKey(validReceiver.tileEntity)) {
                            continue;
                        }
                        reachedTileEntities.put(
                                validReceiver.tileEntity,
                                new EnergyBlockLink(validReceiver.direction)
                        );
                        if (!(validReceiver.tileEntity instanceof IEnergyConductor)) {
                            continue;
                        }
                        tileEntitiesToCheck.remove(validReceiver.tileEntity);
                        tileEntitiesToCheck.add(validReceiver.tileEntity);
                    }
                }
            }
        }
        List<EnergyPath> energyPaths = new LinkedList<>();
        for (Map.Entry<IEnergyTile, EnergyBlockLink> entry : reachedTileEntities.entrySet()) {
            IEnergyTile tileEntity = entry.getKey();
            if (tileEntity instanceof IEnergySink) {
                EnergyBlockLink energyBlockLink = entry.getValue();
                EnergyPath energyPath = new EnergyPath();
                energyPath.target = (IEnergySink) tileEntity;
                energyPath.targetDirection = energyBlockLink.direction;
                if (emitter instanceof IEnergySource) {
                    while (true) {
                        TileEntity te = getTileFromMap(tileEntity);
                        tileEntity = getTileEntity(te.getPos().offset(energyBlockLink.direction));
                        if (tileEntity == emitter) {
                            break;
                        }
                        if (!(tileEntity instanceof IEnergyConductor)) {
                            break;
                        }
                        IEnergyConductor energyConductor = (IEnergyConductor) tileEntity;
                        energyPath.conductors.add(energyConductor);
                        energyPath.loss+=energyConductor.getConductionLoss();
                        if (energyConductor.getInsulationEnergyAbsorption() < energyPath.minInsulationEnergyAbsorption) {
                            energyPath.minInsulationEnergyAbsorption = (int) energyConductor.getInsulationEnergyAbsorption();
                        }
                        if (energyConductor.getInsulationBreakdownEnergy() < energyPath.minInsulationBreakdownEnergy) {
                            energyPath.minInsulationBreakdownEnergy = (int) energyConductor.getInsulationBreakdownEnergy();
                        }
                        if (energyConductor.getConductorBreakdownEnergy() < energyPath.minConductorBreakdownEnergy) {
                            energyPath.minConductorBreakdownEnergy = (int) energyConductor.getConductorBreakdownEnergy();
                        }
                        energyBlockLink = reachedTileEntities.get(tileEntity);

                    }
                }
                energyPaths.add(energyPath);
            }
        }
        return energyPaths;
    }

    private boolean conductorToWeak(Set<IEnergyConductor> par1, double energyToSend) {
        boolean flag = false;
        for (IEnergyConductor cond : par1) {
            if (cond.getConductorBreakdownEnergy() <= energyToSend) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public IEnergyTile getNeighbor(IEnergyTile tile, EnumFacing dir) {
        if (tile == null) {
            return null;
        }
        if (tile instanceof TileEntity) {
            return getTileEntity(((TileEntity) tile).getPos().offset(dir));
        }
        if (tile instanceof ILocatable) {
            TileEntity tile1 = this.world.getTileEntity(((ILocatable) tile).getPosition().offset(dir));
            if (tile1 != null) {
                return getTileEntity(tile1.getPos());
            }
        }
        return null;
    }

    private List<EnergyTarget> getValidReceivers(IEnergyTile emitter, boolean reverse) {
        List<EnergyTarget> validReceivers = new LinkedList<>();
        for (EnumFacing direction : EnumFacing.values()) {
            if (emitter instanceof IMetaDelegate) {
                IMetaDelegate meta = (IMetaDelegate) emitter;
                List<IEnergyTile> targets = meta.getSubTiles();
                for (IEnergyTile tile : targets) {
                    IEnergyTile target = getNeighbor(tile, direction);
                    if (target == emitter) {
                        continue;
                    }
                    if (target == null) {
                        continue;
                    }
                    EnumFacing inverseDirection = direction.getOpposite();
                    if (reverse) {
                        if (!(emitter instanceof IEnergyAcceptor) || !(target instanceof IEnergyEmitter)) {
                            continue;
                        }
                        IEnergyEmitter sender = (IEnergyEmitter) target;
                        IEnergyAcceptor receiver = (IEnergyAcceptor) emitter;
                        if (!sender.emitsEnergyTo(receiver, inverseDirection) || !receiver.acceptsEnergyFrom(sender, direction)) {
                            continue;
                        }
                    } else {
                        if (!(emitter instanceof IEnergyEmitter) || !(target instanceof IEnergyAcceptor)) {
                            continue;
                        }
                        IEnergyEmitter sender = (IEnergyEmitter) emitter;
                        IEnergyAcceptor receiver = (IEnergyAcceptor) target;
                        if (!sender.emitsEnergyTo(receiver, direction) || !receiver.acceptsEnergyFrom(sender, inverseDirection)) {
                            continue;
                        }
                    }
                    validReceivers.add(new EnergyTarget(target, inverseDirection));
                }
            } else {
                IEnergyTile target2 = getNeighbor(emitter, direction);
                if (target2 != null) {
                    EnumFacing inverseDirection2 = direction.getOpposite();
                    if (reverse) {
                        if (emitter instanceof IEnergyAcceptor && target2 instanceof IEnergyEmitter) {
                            IEnergyEmitter sender2 = (IEnergyEmitter) target2;
                            IEnergyAcceptor receiver2 = (IEnergyAcceptor) emitter;
                            if (sender2.emitsEnergyTo(receiver2, inverseDirection2) && receiver2.acceptsEnergyFrom(
                                    sender2,
                                    direction
                            )) {
                                validReceivers.add(new EnergyTarget(target2, inverseDirection2));
                            }
                        }
                    } else if (emitter instanceof IEnergyEmitter && target2 instanceof IEnergyAcceptor) {
                        IEnergyEmitter sender2 = (IEnergyEmitter) emitter;
                        IEnergyAcceptor receiver2 = (IEnergyAcceptor) target2;
                        if (sender2.emitsEnergyTo(receiver2, direction) && receiver2.acceptsEnergyFrom(
                                sender2,
                                inverseDirection2
                        )) {
                            validReceivers.add(new EnergyTarget(target2, inverseDirection2));
                        }
                    }
                }
            }
        }
        return validReceivers;
    }

    public List<IEnergySource> discoverFirstPathOrSources(IEnergyTile par1) {
        Set<IEnergyTile> reached = new HashSet<>();
        List<IEnergySource> result = new ArrayList<>();
        List<IEnergyTile> workList = new ArrayList<>();
        workList.add(par1);
        while (workList.size() > 0) {
            IEnergyTile tile = workList.remove(0);
            TileEntity te = getTileFromMap(tile);
            if (!te.isInvalid()) {
                List<EnergyTarget> targets = getValidReceivers(tile, true);
                for (EnergyTarget energyTarget : targets) {
                    IEnergyTile target = energyTarget.tileEntity;
                    if (target != par1 &&
                            !reached.contains(target)) {
                        reached.add(target);
                        if (target instanceof IEnergySource) {
                            result.add((IEnergySource) target);
                            continue;
                        }
                        if (target instanceof IEnergyConductor) {
                            workList.add(target);
                        }
                    }
                }
            }
        }
        return result;
    }

    public void onTickStart() {

    }

    public void onTickEnd() {
        if (this.world.provider.getWorldTime() % Config.tickupdateenergysystem == 0) {
            if (this.waitingList.hasWork()) {
                List<IEnergyTile> tiles = this.waitingList.getPathTiles();
                for (IEnergyTile tile : tiles) {
                    List<IEnergySource> sources = discoverFirstPathOrSources(tile);
                    if (sources.size() > 0) {
                        this.energySourceToEnergyPathMap.removeAll(sources);
                    }
                }
                this.waitingList.clear();
            }
        }
        if (this.world.provider.getWorldTime() % Config.ticktransferenergy == 0) {
            for (IEnergySource entry : this.sources) {
                if (entry != null) {
                    double offer = Math.min(entry
                            .getOfferedEnergy(), EnergyNet.instance
                            .getPowerFromTier(entry.getSourceTier()));
                    if (offer > 0.0D) {
                        for (int i = 0; i < getPacketAmount(entry); i++) {

                            offer = Math.min(entry
                                    .getOfferedEnergy(), EnergyNet.instance
                                    .getPowerFromTier(entry.getSourceTier()));
                            if (offer < 1) {
                                break;
                            }
                            double removed = offer - emitEnergyFrom(entry, offer);
                            if (removed <= 0.0D) {
                                break;
                            }
                            entry.drawEnergy(removed);
                        }
                    }
                }
            }
        }
    }

    private double getPacketAmount(IEnergySource source) {
        if (source instanceof TileEntityTransformer) {
            NBTTagCompound nbt = ((TileEntityTransformer) (source)).writeToNBT(new NBTTagCompound());
            TileEntityTransformer.Mode red = TileEntityTransformer.Mode.redstone;
            int mode = nbt.getInteger("mode");
            if (mode >= 0 && mode < TileEntityTransformer.Mode.values().length) {
                red = TileEntityTransformer.Mode.values()[mode];
            }
            return (red == ic2.core.block.wiring.TileEntityTransformer.Mode.stepdown) ? 1.0D : 4.0D;
        }
        if (source instanceof com.denfop.tiles.base.TileEntityTransformer) {
            return ((com.denfop.tiles.base.TileEntityTransformer) source).isStepUp() ? 1.0D : 4.0D;
        }
        return 1.0D;
    }

    public void explodeTiles(IEnergySink sink) {
        assert !(sink instanceof IEnergySource);
        removeTile(sink);
        if (sink instanceof IMetaDelegate) {
            IMetaDelegate meta = (IMetaDelegate) sink;
            for (IEnergyTile tile : meta.getSubTiles()) {
                explodeMachineAt(getTileFromIEnergy(tile).getPos());
            }
        } else {
            explodeMachineAt(getTileFromIEnergy(sink).getPos());
        }
    }

    void explodeMachineAt(BlockPos pos) {
        if (Config.enableexlposion) {
            this.world.setBlockToAir(pos);
            float power = 1.0F;
            ExplosionIC2 explosion = new ExplosionIC2(
                    this.world,
                    null,
                    0.5D + pos.getX(),
                    0.5D + pos.getY(),
                    0.5D + pos.getZ(),
                    power,
                    0.75F
            );
            explosion.doExplosion();
        }
    }

    public IEnergyTile getTileEntity(BlockPos pos) {
        return this.chunkCoordinatesIEnergyTileMap.get(pos);
    }

    public NodeStats getNodeStats(IEnergyTile tile) {
        double emitted = getTotalEnergyEmitted(tile);
        double received = getTotalEnergySunken(tile);
        return new NodeStats(received, emitted, EnergyNet.instance.getTierFromPower(getVoltage(tile)));
    }

    private double getVoltage(IEnergyTile tileEntity) {
        double voltage = 0.0D;
        if (tileEntity instanceof IEnergySource && this.energySourceToEnergyPathMap.containsKey((IEnergySource) tileEntity)) {
            for (EnergyPath energyPath2 : this.energySourceToEnergyPathMap.get((IEnergySource) tileEntity)) {
                voltage = Math.max(voltage, energyPath2.maxSendedEnergy);
            }
        }
        if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink) {
            for (EnergyPath energyPath3 : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity)) {
                if ((tileEntity instanceof IEnergySink && energyPath3.target == tileEntity) || (tileEntity instanceof IEnergyConductor && energyPath3.conductors.contains(
                        tileEntity))) {
                    voltage = Math.max(voltage, energyPath3.maxSendedEnergy);
                }
            }
        }
        return voltage;
    }

    void update(int x, int y, int z) {
        for (EnumFacing dir : EnumFacing.values()) {
            if (this.world.isChunkGeneratedAt(x + dir.getFrontOffsetX() >> 4, z + dir.getFrontOffsetZ() >> 4)) {
                BlockPos pos = (new BlockPos(x, y, z)).offset(dir);
                this.world.neighborChanged(pos, Blocks.AIR, pos);
            }
        }
    }

    public void onUnload() {
        this.energySourceToEnergyPathMap.clear();
        this.sources.clear();
        this.waitingList.clear();
        this.chunkCoordinatesIEnergyTileMap.clear();
        this.chunkCoordinatesMap.clear();
    }

    static class EnergyTarget {

        final IEnergyTile tileEntity;

        final EnumFacing direction;

        EnergyTarget(IEnergyTile tileEntity, EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }

    }

    static class EnergyBlockLink {

        final EnumFacing direction;
        EnergyBlockLink(EnumFacing direction) {
            this.direction = direction;
        }

    }

    static class EnergyPath {

        final Set<IEnergyConductor> conductors = new HashSet<>();
        IEnergySink target = null;
        EnumFacing targetDirection;
        double loss = 0.0D;

        double minInsulationEnergyAbsorption = 2.147483647E9D;

        double minInsulationBreakdownEnergy = 2.147483647E9D;

        double minConductorBreakdownEnergy = 2.147483647E9D;

        long totalEnergyConducted = 0L;

        long maxSendedEnergy = 0L;

    }

    static class EnergyPathMap {

        final Map<IEnergySource, List<EnergyPath>> senderPath;

        EnergyPathMap() {
            this.senderPath = new HashMap<>();

        }

        public void put(final IEnergySource par1, final List<EnergyPath> par2) {
            this.senderPath.put(par1, par2);


        }

        public boolean containsKey(final IEnergySource par1) {
            return this.senderPath.containsKey(par1);
        }

        public List<EnergyPath> get(final IEnergySource par1) {
            return this.senderPath.get(par1);
        }

        public void remove(final IEnergySource par1) {
            this.senderPath.remove(par1);
        }

        public void removeAll(final List<IEnergySource> par1) {
            for (IEnergySource iEnergySource : par1) {
                this.remove(iEnergySource);
            }
        }

        public List<EnergyPath> getPaths(final IEnergyAcceptor par1) {
            final List<EnergyPath> paths = new ArrayList<>();
            for (final IEnergySource source : this.getSources(par1)) {
                if (this.containsKey(source)) {
                    paths.addAll(this.get(source));
                }
            }
            return paths;
        }

        public List<IEnergySource> getSources(final IEnergyAcceptor par1) {
            final List<IEnergySource> source = new ArrayList<>();
            for (final Map.Entry<IEnergySource, List<EnergyPath>> entry : this.senderPath.entrySet()) {
                if (source.contains(entry.getKey())) {
                    continue;
                }
                for (EnergyPath path : entry.getValue()) {
                    if ((!(par1 instanceof IEnergyConductor) || !path.conductors.contains(par1)) && (!(par1 instanceof IEnergySink) || path.target != par1)) {
                        continue;
                    }
                    source.add(entry.getKey());
                }
            }
            return source;
        }

        public void clear() {
            this.senderPath.clear();
        }

    }

    static class PathLogic {

        final List<IEnergyTile> tiles = new ArrayList<>();

        public boolean contains(IEnergyTile par1) {
            return this.tiles.contains(par1);
        }

        public void add(IEnergyTile par1) {
            this.tiles.add(par1);
        }

        public void remove(IEnergyTile par1) {
            this.tiles.remove(par1);
        }

        public void clear() {
            this.tiles.clear();
        }

        public IEnergyTile getRepresentingTile() {
            if (this.tiles.isEmpty()) {
                return null;
            }
            return this.tiles.get(0);
        }

    }

    class WaitingList {

        final List<EnergyNetLocal.PathLogic> paths = new ArrayList<>();

        public void onTileEntityAdded(List<EnergyNetLocal.EnergyTarget> around, IEnergyTile tile) {
            if (around.isEmpty() || this.paths.isEmpty()) {
                createNewPath(tile);
                return;
            }
            boolean found = false;
            List<EnergyNetLocal.PathLogic> logics = new ArrayList<>();
            for (EnergyNetLocal.PathLogic logic : this.paths) {
                if (logic.contains(tile)) {
                    found = true;
                    if (tile instanceof IEnergyConductor) {
                        logics.add(logic);
                    }
                    continue;
                }
                for (EnergyNetLocal.EnergyTarget target : around) {
                    if (logic.contains(target.tileEntity)) {
                        found = true;
                        logic.add(tile);
                        if (target.tileEntity instanceof IEnergyConductor) {
                            logics.add(logic);
                        }
                    }
                }
            }
            if (logics.size() > 1 && tile instanceof IEnergyConductor) {
                EnergyNetLocal.PathLogic newLogic = new EnergyNetLocal.PathLogic();
                for (EnergyNetLocal.PathLogic logic2 : logics) {
                    this.paths.remove(logic2);
                    for (IEnergyTile toMove : logic2.tiles) {
                        if (!newLogic.contains(toMove)) {
                            newLogic.add(toMove);
                        }
                    }
                    logic2.clear();
                }
                this.paths.add(newLogic);
            }
            if (!found) {
                createNewPath(tile);
            }
        }

        public void onTileEntityRemoved(IEnergyTile par1) {
            if (this.paths.isEmpty()) {
                return;
            }
            List<IEnergyTile> toRecalculate = new ArrayList<>();
            for (int i = 0; i < this.paths.size(); i++) {
                EnergyNetLocal.PathLogic logic = this.paths.get(i);
                if (logic.contains(par1)) {
                    logic.remove(par1);
                    toRecalculate.addAll(logic.tiles);
                    this.paths.remove(i--);
                }
            }
            for (IEnergyTile tile : toRecalculate) {
                onTileEntityAdded(EnergyNetLocal.this.getValidReceivers(tile, true), tile);
            }
        }

        public void createNewPath(IEnergyTile par1) {
            EnergyNetLocal.PathLogic logic = new EnergyNetLocal.PathLogic();
            logic.add(par1);
            this.paths.add(logic);
        }

        public void clear() {
            if (this.paths.isEmpty()) {
                return;
            }

            this.paths.clear();
        }

        public boolean hasWork() {
            return (this.paths.size() > 0);
        }

        public List<IEnergyTile> getPathTiles() {
            List<IEnergyTile> tiles = new ArrayList<>();
            for (EnergyNetLocal.PathLogic path : this.paths) {
                IEnergyTile tile = path.getRepresentingTile();
                if (tile != null) {
                    tiles.add(tile);
                }
            }
            return tiles;
        }

    }

}
