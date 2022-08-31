package com.denfop.heat;

import com.denfop.api.heat.IHeatAcceptor;
import com.denfop.api.heat.IHeatConductor;
import com.denfop.api.heat.IHeatEmitter;
import com.denfop.api.heat.IHeatSink;
import com.denfop.api.heat.IHeatSource;
import com.denfop.api.heat.IHeatTile;
import com.denfop.tiles.mechanism.dual.heat.TileEntityAlloySmelter;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
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

public class  HeatNetLocal {

    private static EnumFacing[] directions;

    static {
        HeatNetLocal.directions = EnumFacing.values();
    }

    private final World world;
    private final HeatPathMap heatSourceToHeatPathMap;
    private final Map<IHeatTile, BlockPos> chunkCoordinatesMap;
    private final Map<IHeatTile, TileEntity> heatTileTileEntityMap;

    private final Map<BlockPos, IHeatTile> chunkCoordinatesIHeatTileMap;
    private final List<IHeatSource> sources;
    private final WaitingList waitingList;

    HeatNetLocal(final World world) {
        this.heatSourceToHeatPathMap = new HeatPathMap();
        this.sources = new ArrayList<>();
        this.waitingList = new WaitingList();
        this.world = world;
        this.chunkCoordinatesIHeatTileMap = new HashMap<>();
        this.chunkCoordinatesMap = new HashMap<>();
        this.heatTileTileEntityMap = new HashMap<>();
    }

    public void addTile(IHeatTile tile1) {


        this.addTileEntity(getTileFromIHeat(tile1).getPos(), tile1);


    }

    public BlockPos getPos(final IHeatTile tile) {
        return this.chunkCoordinatesMap.get(tile);
    }

    public void addTileEntity(final BlockPos coords, final IHeatTile tile) {
        if (this.chunkCoordinatesIHeatTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromIHeat(tile);
        this.heatTileTileEntityMap.put(tile, te);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesIHeatTileMap.put(coords, tile);
        this.update(coords);
        if (tile instanceof IHeatAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), (IHeatAcceptor) tile);
        }
        if (tile instanceof IHeatSource) {
            this.sources.add((IHeatSource) tile);
        }
    }

    public void removeTile(IHeatTile tile1) {

        this.removeTileEntity(tile1);

    }

    public void removeTileEntity(BlockPos coord, IHeatTile tile, IHeatTile tile1) {
        if (!this.chunkCoordinatesIHeatTileMap.containsKey(coord)) {
            return;
        }
        this.chunkCoordinatesMap.remove(tile, coord);

        this.chunkCoordinatesIHeatTileMap.remove(coord);
        this.heatTileTileEntityMap.remove(tile1, this.heatTileTileEntityMap.get(tile1));
        this.heatTileTileEntityMap.remove(tile, this.heatTileTileEntityMap.get(tile));
        this.update(coord);
        if (tile instanceof IHeatAcceptor) {
            this.heatSourceToHeatPathMap.removeAll(this.heatSourceToHeatPathMap.getSources((IHeatAcceptor) tile));
            this.waitingList.onTileEntityRemoved((IHeatAcceptor) tile);
        }
        if (tile instanceof IHeatSource) {
            this.sources.remove((IHeatSource) tile);
            this.heatSourceToHeatPathMap.remove((IHeatSource) tile);
        }
    }

    public void removeTileEntity(IHeatTile tile) {
        if (!this.heatTileTileEntityMap.containsKey(tile)) {
            return;
        }
        final BlockPos coord = this.chunkCoordinatesMap.get(tile);
        this.chunkCoordinatesMap.remove(tile);
        this.heatTileTileEntityMap.remove(tile, this.heatTileTileEntityMap.get(tile));
        this.chunkCoordinatesIHeatTileMap.remove(coord, tile);
        this.update(coord);
        if (tile instanceof IHeatAcceptor) {
            this.heatSourceToHeatPathMap.removeAll(this.heatSourceToHeatPathMap.getSources((IHeatAcceptor) tile));
            this.waitingList.onTileEntityRemoved((IHeatAcceptor) tile);
        }
        if (tile instanceof IHeatSource) {
            this.sources.remove((IHeatSource) tile);
            this.heatSourceToHeatPathMap.remove((IHeatSource) tile);
        }
    }

    public TileEntity getTileFromMap(IHeatTile tile) {
        return this.heatTileTileEntityMap.get(tile);
    }

    public double emitHeatFrom(final IHeatSource heatSource, double amount) {
        List<HeatPath> heatPaths = this.heatSourceToHeatPathMap.get(heatSource);
        if (heatPaths == null) {
            heatPaths = this.discover(heatSource);
            this.heatSourceToHeatPathMap.put(heatSource, heatPaths);
        }
        boolean allowed = heatSource.isAllowed();
        boolean need = false;
        if (amount > 0 || !allowed) {
            for (final HeatPath heatPath : heatPaths) {
                final IHeatSink heatSink = heatPath.target;
                if(heatSink.needTemperature())
                    if(!allowed) {
                        heatSource.setAllowed(true);
                    }
                need |=heatSink.needTemperature();
                double demandedHeat = heatSink.getDemandedHeat();
                if (demandedHeat <= 0.0) {
                    continue;
                }
                double heatProvided = Math.floor(Math.round(amount));
                double adding = Math.min(heatProvided, demandedHeat);
                if (adding <= 0.0D) {
                    continue;
                }
                heatSink.injectHeat(heatPath.targetDirection, adding, 0);
                heatPath.totalHeatConducted = (long) adding;
                if (adding > heatPath.min) {
                    heatPath.conductors.forEach(conductor -> {
                        if (conductor.getConductorBreakdownEnergy() - 1 < heatPath.min) {
                            conductor.removeConductor();
                        }
                    });
                }


            }
        }
        if(!need)
            heatSource.setAllowed(false);
        return amount;
    }


    public TileEntity getTileFromIHeat(IHeatTile tile) {
        if (tile instanceof TileEntity) {
            return (TileEntity) tile;
        }
        if (tile instanceof ILocatable) {
            return this.world.getTileEntity(((ILocatable) tile).getPosition());
        }

        return null;
    }

    private List<HeatPath> discover(final IHeatSource emitter) {
        final Map<IHeatConductor, EnumFacing> reachedTileEntities = new HashMap<>();
        final List<IHeatTile> tileEntitiesToCheck = new ArrayList<>();
        final List<HeatPath> heatPaths = new ArrayList<>();

        tileEntitiesToCheck.add(emitter);

        while (!tileEntitiesToCheck.isEmpty()) {
            final IHeatTile currentTileEntity = tileEntitiesToCheck.remove(0);
            final List<HeatTarget> validReceivers = this.getValidReceivers(currentTileEntity, false);
            for (final HeatTarget validReceiver : validReceivers) {
                if (validReceiver.tileEntity != emitter) {
                    if (validReceiver.tileEntity instanceof IHeatSink) {
                        heatPaths.add(new HeatPath((IHeatSink) validReceiver.tileEntity, validReceiver.direction));
                        continue;
                    }
                    if (reachedTileEntities.containsKey((IHeatConductor) validReceiver.tileEntity)) {
                        continue;
                    }

                    reachedTileEntities.put((IHeatConductor) validReceiver.tileEntity, validReceiver.direction);
                    tileEntitiesToCheck.add(validReceiver.tileEntity);
                }
            }


        }
        for (HeatPath heatPath : heatPaths) {
            IHeatTile tileEntity = heatPath.target;
            EnumFacing heatBlockLink = heatPath.targetDirection;
            if (emitter != null) {
                while (tileEntity != emitter) {
                    BlockPos te = this.chunkCoordinatesMap.get(tileEntity);
                    if (heatBlockLink != null && te != null) {
                        tileEntity = this.getTileEntity(te.offset(heatBlockLink));
                    }
                    if (!(tileEntity instanceof IHeatConductor)) {
                        break;
                    }
                    final IHeatConductor heatConductor = (IHeatConductor) tileEntity;
                    heatPath.conductors.add(heatConductor);
                    if (heatConductor.getConductorBreakdownEnergy() - 1 < heatPath.getMin()) {
                        heatPath.setMin(heatConductor.getConductorBreakdownEnergy() - 1);
                    }

                    heatBlockLink = reachedTileEntities.get(tileEntity);
                    if (heatBlockLink != null) {
                        continue;
                    }
                    assert te != null;
                    IC2.platform.displayError("An heat network pathfinding entry is corrupted.\nThis could happen due to " +
                            "incorrect Minecraft behavior or a bug.\n\n(Technical information: heatBlockLink, tile " +
                            "entities below)\nE: " + emitter + " (" + te.getX() + "," + te.getY() + "," + te

                            .getZ() + ")\n" + "C: " + tileEntity + " (" + te.getX() + "," + te

                            .getY() + "," + te

                            .getZ() + ")\n" + "R: " + heatPath.target + " (" + this.heatTileTileEntityMap
                            .get(heatPath.target)
                            .getPos()
                            .getX() + "," + getTileFromMap(heatPath.target).getPos().getY() + "," + getTileFromIHeat(
                            heatPath.target).getPos().getZ() + ")");
                }
            }
        }
        return heatPaths;
    }

    public IHeatTile getNeighbor(final IHeatTile tile, final EnumFacing dir) {
        if (tile == null) {
            return null;
        }
        if (!this.heatTileTileEntityMap.containsKey(tile)) {
            return null;
        }
        return this.getTileEntity(this.heatTileTileEntityMap.get(tile).getPos().offset(dir));
    }

    private List<HeatTarget> getValidReceivers(final IHeatTile emitter, final boolean reverse) {
        final List<HeatTarget> validReceivers = new LinkedList<>();

        for (final EnumFacing direction : HeatNetLocal.directions) {
            final IHeatTile target2 = getNeighbor(emitter, direction);
            if (target2 != null) {
                final EnumFacing inverseDirection2 = direction.getOpposite();
                if (reverse) {
                    if (emitter instanceof IHeatAcceptor && target2 instanceof IHeatEmitter) {
                        final IHeatEmitter sender2 = (IHeatEmitter) target2;
                        final IHeatAcceptor receiver2 = (IHeatAcceptor) emitter;
                        if (sender2.emitsHeatTo(receiver2, inverseDirection2) && receiver2.acceptsHeatFrom(
                                sender2,
                                direction
                        )) {
                            validReceivers.add(new HeatTarget(target2, inverseDirection2));
                        }
                    }
                } else if (emitter instanceof IHeatEmitter && target2 instanceof IHeatAcceptor) {
                    final IHeatEmitter sender2 = (IHeatEmitter) emitter;
                    final IHeatAcceptor receiver2 = (IHeatAcceptor) target2;
                    if (sender2.emitsHeatTo(receiver2, direction) && receiver2.acceptsHeatFrom(
                            sender2,
                            inverseDirection2
                    )) {
                        validReceivers.add(new HeatTarget(target2, inverseDirection2));
                    }
                }
            }
        }


        return validReceivers;
    }

    public List<IHeatSource> discoverFirstPathOrSources(final IHeatTile par1) {
        final Set<IHeatTile> reached = new HashSet<>();
        final List<IHeatSource> result = new ArrayList<>();
        final List<IHeatTile> workList = new ArrayList<>();
        workList.add(par1);
        while (workList.size() > 0) {
            final IHeatTile tile = workList.remove(0);

            final List<HeatTarget> targets = this.getValidReceivers(tile, true);
            for (HeatTarget heatTarget : targets) {
                final IHeatTile target = heatTarget.tileEntity;
                if (target != par1) {
                    if (!reached.contains(target)) {
                        reached.add(target);
                        if (target instanceof IHeatSource) {
                            result.add((IHeatSource) target);
                        } else if (target instanceof IHeatConductor) {
                            workList.add(target);
                        }
                    }
                }
            }
        }
        return result;
    }


    public void onTickEnd() {
        if (this.world.provider.getWorldTime() % 20 == 0) {
            if (this.waitingList.hasWork()) {
                final List<IHeatTile> tiles = this.waitingList.getPathTiles();
                for (final IHeatTile tile : tiles) {
                    final List<IHeatSource> sources = this.discoverFirstPathOrSources(tile);
                    if (sources.size() > 0) {
                        this.heatSourceToHeatPathMap.removeAll(sources);
                    }
                }
                this.waitingList.clear();
            }
        }
        for (IHeatSource entry : this.sources) {
            if (entry != null) {
                final double offered = entry.getOfferedHeat();
                if (offered > 0 || !entry.isAllowed()) {
                    for (double packetAmount = 1, i = 0; i < packetAmount; ++i) {
                        final double removed = offered - this.emitHeatFrom(entry, offered);
                        if (removed <= 0) {
                            break;
                        }

                        entry.drawHeat(removed);
                    }
                }

            }
        }
    }

    public IHeatTile getTileEntity(BlockPos pos) {

        return this.chunkCoordinatesIHeatTileMap.get(pos);
    }


    void update(BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.values()) {
            BlockPos pos1 = pos
                    .offset(dir);
            final IHeatTile tile = this.chunkCoordinatesIHeatTileMap.get(pos1);
            if (tile != null) {
                if (tile instanceof IHeatConductor) {
                    ((IHeatConductor) tile).update_render();
                }
            }

        }
    }

    public void onUnload() {
        this.heatSourceToHeatPathMap.clear();
        this.sources.clear();
        this.waitingList.clear();
        this.chunkCoordinatesIHeatTileMap.clear();
        this.chunkCoordinatesMap.clear();
        this.heatTileTileEntityMap.clear();
    }

    static class HeatTarget {

        final IHeatTile tileEntity;
        final EnumFacing direction;

        HeatTarget(final IHeatTile tileEntity, final EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }

    }

    static class HeatPath {

        final Set<IHeatConductor> conductors;
        final IHeatSink target;
        final EnumFacing targetDirection;
        long totalHeatConducted;
        double min = Double.MAX_VALUE;

        HeatPath(IHeatSink sink, EnumFacing facing) {
            this.target = sink;
            this.conductors = new HashSet<>();
            this.totalHeatConducted = 0L;
            this.targetDirection = facing;
        }

        public double getMin() {
            return min;
        }

        public void setMin(final double min) {
            this.min = min;
        }

    }


    static class HeatPathMap {

        final Map<IHeatSource, List<HeatPath>> senderPath;

        HeatPathMap() {
            this.senderPath = new HashMap<>();
        }

        public void put(final IHeatSource par1, final List<HeatPath> par2) {
            this.senderPath.put(par1, par2);
        }


        public boolean containsKey(final IHeatSource par1) {
            return this.senderPath.containsKey(par1);
        }

        public List<HeatPath> get(final IHeatSource par1) {
            return this.senderPath.get(par1);
        }


        public void remove(final IHeatSource par1) {
            this.senderPath.remove(par1);
        }

        public void removeAll(final List<IHeatSource> par1) {
            if (par1 == null) {
                return;
            }
            for (IHeatSource iHeatSource : par1) {
                this.remove(iHeatSource);
            }
        }


        public List<IHeatSource> getSources(final IHeatAcceptor par1) {
            final List<IHeatSource> source = new ArrayList<>();
            for (final Map.Entry<IHeatSource, List<HeatPath>> entry : this.senderPath.entrySet()) {
                if (source.contains(entry.getKey())) {
                    continue;
                }
                for (HeatPath path : entry.getValue()) {
                    if ((!(par1 instanceof IHeatConductor) || !path.conductors.contains(par1)) && (!(par1 instanceof IHeatSink) || path.target != par1)) {
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

        final Set<IHeatTile> tiles;

        PathLogic() {
            this.tiles = new HashSet<>();
        }

        public boolean contains(final IHeatTile par1) {
            return this.tiles.contains(par1);
        }

        public void add(final IHeatTile par1) {
            this.tiles.add(par1);
        }

        public void remove(final IHeatTile par1) {
            this.tiles.remove(par1);
        }

        public void clear() {
            this.tiles.clear();
        }

        public IHeatTile getRepresentingTile() {
            if (this.tiles.isEmpty()) {
                return null;
            }
            return this.tiles.iterator().next();
        }

    }

    class WaitingList {

        final List<PathLogic> paths;

        WaitingList() {
            this.paths = new ArrayList<>();
        }

        public void onTileEntityAdded(final List<HeatTarget> around, final IHeatAcceptor tile) {
            if (around.isEmpty() || this.paths.isEmpty()) {
                this.createNewPath(tile);
                return;
            }
            boolean found = false;
            final List<PathLogic> logics = new ArrayList<>();
            for (final PathLogic logic : this.paths) {
                if (logic.contains(tile)) {
                    found = true;
                    if (tile instanceof IHeatConductor) {
                        logics.add(logic);
                    }
                } else {
                    for (final HeatTarget target : around) {
                        if (logic.contains(target.tileEntity)) {
                            found = true;
                            logic.add(tile);
                            if (target.tileEntity instanceof IHeatConductor) {
                                logics.add(logic);
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (logics.size() > 1 && tile instanceof IHeatConductor) {
                final PathLogic newLogic = new PathLogic();
                for (final PathLogic logic2 : logics) {
                    this.paths.remove(logic2);
                    for (final IHeatTile toMove : logic2.tiles) {
                        if (!newLogic.contains(toMove)) {
                            newLogic.add(toMove);
                        }
                    }
                }
                this.paths.add(newLogic);
            }
            if (!found) {
                this.createNewPath(tile);
            }
        }

        public void onTileEntityRemoved(final IHeatAcceptor par1) {
            if (this.paths.isEmpty()) {
                return;
            }

            List<IHeatTile> toRecalculate = new ArrayList<>();
            for (int i = 0; i < this.paths.size(); i++) {
                PathLogic logic = this.paths.get(i);
                if (logic.contains(par1)) {
                    logic.remove(par1);
                    toRecalculate.addAll(logic.tiles);
                    this.paths.remove(i--);
                }
            }
            for (final IHeatTile tile : toRecalculate) {
                this.onTileEntityAdded(HeatNetLocal.this.getValidReceivers(tile, true), (IHeatAcceptor) tile);
            }
        }

        public void createNewPath(final IHeatTile par1) {
            final PathLogic logic = new PathLogic();
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
            return this.paths.size() > 0;
        }

        public List<IHeatTile> getPathTiles() {
            final List<IHeatTile> tiles = new ArrayList<>();
            for (PathLogic path : this.paths) {
                final IHeatTile tile = path.getRepresentingTile();
                if (tile != null) {
                    tiles.add(tile);
                }
            }
            return tiles;
        }

    }

}
