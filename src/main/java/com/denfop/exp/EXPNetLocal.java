package com.denfop.exp;

import com.denfop.api.exp.IEXPAcceptor;
import com.denfop.api.exp.IEXPConductor;
import com.denfop.api.exp.IEXPEmitter;
import com.denfop.api.exp.IEXPSink;
import com.denfop.api.exp.IEXPSource;
import com.denfop.api.exp.IEXPTile;
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

public class EXPNetLocal {

    private static EnumFacing[] directions;

    static {
        EXPNetLocal.directions = EnumFacing.values();
    }

    private final World world;
    private final EXPPathMap expSourceToEXPPathMap;
    private final Map<IEXPTile, BlockPos> chunkCoordinatesMap;
    private final Map<IEXPTile, TileEntity> expTileTileEntityMap;

    private final Map<BlockPos, IEXPTile> chunkCoordinatesIEXPTileMap;
    private final List<IEXPSource> sources;
    private final WaitingList waitingList;

    EXPNetLocal(final World world) {
        this.expSourceToEXPPathMap = new EXPPathMap();
        this.sources = new ArrayList<>();
        this.waitingList = new WaitingList();
        this.world = world;
        this.chunkCoordinatesIEXPTileMap = new HashMap<>();
        this.chunkCoordinatesMap = new HashMap<>();
        this.expTileTileEntityMap = new HashMap<>();
    }

    public void addTile(IEXPTile tile1) {


        this.addTileEntity(getTileFromIEXP(tile1).getPos(), tile1);


    }

    public BlockPos getPos(final IEXPTile tile) {
        return this.chunkCoordinatesMap.get(tile);
    }

    public void addTileEntity(final BlockPos coords, final IEXPTile tile) {
        if (this.chunkCoordinatesIEXPTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromIEXP(tile);
        this.expTileTileEntityMap.put(tile, te);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesIEXPTileMap.put(coords, tile);
        this.update(coords);
        if (tile instanceof IEXPAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), (IEXPAcceptor) tile);
        }
        if (tile instanceof IEXPSource) {
            this.sources.add((IEXPSource) tile);
        }
    }

    public void removeTile(IEXPTile tile1) {

        this.removeTileEntity(tile1);

    }

    public void removeTileEntity(BlockPos coord, IEXPTile tile, IEXPTile tile1) {
        if (!this.chunkCoordinatesIEXPTileMap.containsKey(coord)) {
            return;
        }
        this.chunkCoordinatesMap.remove(tile, coord);

        this.chunkCoordinatesIEXPTileMap.remove(coord);
        this.expTileTileEntityMap.remove(tile1, this.expTileTileEntityMap.get(tile1));
        this.expTileTileEntityMap.remove(tile, this.expTileTileEntityMap.get(tile));
        this.update(coord);
        if (tile instanceof IEXPAcceptor) {
            this.expSourceToEXPPathMap.removeAll(this.expSourceToEXPPathMap.getSources((IEXPAcceptor) tile));
            this.waitingList.onTileEntityRemoved((IEXPAcceptor) tile);
        }
        if (tile instanceof IEXPSource) {
            this.sources.remove((IEXPSource) tile);
            this.expSourceToEXPPathMap.remove((IEXPSource) tile);
        }
    }

    public void removeTileEntity(IEXPTile tile) {
        if (!this.expTileTileEntityMap.containsKey(tile)) {
            return;
        }
        final BlockPos coord = this.chunkCoordinatesMap.get(tile);
        this.chunkCoordinatesMap.remove(tile);
        this.expTileTileEntityMap.remove(tile, this.expTileTileEntityMap.get(tile));
        this.chunkCoordinatesIEXPTileMap.remove(coord, tile);
        this.update(coord);
        if (tile instanceof IEXPAcceptor) {
            this.expSourceToEXPPathMap.removeAll(this.expSourceToEXPPathMap.getSources((IEXPAcceptor) tile));
            this.waitingList.onTileEntityRemoved((IEXPAcceptor) tile);
        }
        if (tile instanceof IEXPSource) {
            this.sources.remove((IEXPSource) tile);
            this.expSourceToEXPPathMap.remove((IEXPSource) tile);
        }
    }

    public TileEntity getTileFromMap(IEXPTile tile) {
        return this.expTileTileEntityMap.get(tile);
    }

    public double emitEXPFrom(final IEXPSource expSource, double amount) {
        List<EXPPath> expPaths = this.expSourceToEXPPathMap.get(expSource);
        if (expPaths == null) {
            expPaths = this.discover(expSource);
            this.expSourceToEXPPathMap.put(expSource, expPaths);
        }
        if (amount > 0) {
            for (final EXPPath expPath : expPaths) {
                if (amount <= 0) {
                    break;
                }
                final IEXPSink expSink = expPath.target;
                double demandedEXP = expSink.getDemandedEXP();
                if (demandedEXP <= 0.0) {
                    continue;
                }
                double expProvided = Math.floor(Math.round(amount));
                double adding = Math.min(expProvided, demandedEXP);
                if (adding <= 0.0D) {
                    continue;
                }
                expSink.injectEXP(expPath.targetDirection, adding, 0);
                expPath.totalEXPConducted = (long) adding;

                amount -= adding;
                amount = Math.max(0, amount);


            }
        }

        return amount;
    }


    public TileEntity getTileFromIEXP(IEXPTile tile) {
        if (tile instanceof TileEntity) {
            return (TileEntity) tile;
        }
        if (tile instanceof ILocatable) {
            return this.world.getTileEntity(((ILocatable) tile).getPosition());
        }

        return null;
    }

    private List<EXPPath> discover(final IEXPSource emitter) {
        final Map<IEXPConductor, EnumFacing> reachedTileEntities = new HashMap<>();
        final List<IEXPTile> tileEntitiesToCheck = new ArrayList<>();
        final List<EXPPath> expPaths = new ArrayList<>();

        tileEntitiesToCheck.add(emitter);

        while (!tileEntitiesToCheck.isEmpty()) {
            final IEXPTile currentTileEntity = tileEntitiesToCheck.remove(0);
            final List<EXPTarget> validReceivers = this.getValidReceivers(currentTileEntity, false);
            for (final EXPTarget validReceiver : validReceivers) {
                if (validReceiver.tileEntity != emitter) {
                    if (validReceiver.tileEntity instanceof IEXPSink) {
                        expPaths.add(new EXPPath((IEXPSink) validReceiver.tileEntity, validReceiver.direction));
                        continue;
                    }
                    if (reachedTileEntities.containsKey((IEXPConductor) validReceiver.tileEntity)) {
                        continue;
                    }

                    reachedTileEntities.put((IEXPConductor) validReceiver.tileEntity, validReceiver.direction);
                    tileEntitiesToCheck.add(validReceiver.tileEntity);
                }
            }


        }
        for (EXPPath expPath : expPaths) {
            IEXPTile tileEntity = expPath.target;
            EnumFacing expBlockLink = expPath.targetDirection;
            if (emitter != null) {
                while (tileEntity != emitter) {
                    BlockPos te = this.chunkCoordinatesMap.get(tileEntity);
                    if (expBlockLink != null && te != null) {
                        tileEntity = this.getTileEntity(te.offset(expBlockLink));
                    }
                    if (!(tileEntity instanceof IEXPConductor)) {
                        break;
                    }
                    final IEXPConductor expConductor = (IEXPConductor) tileEntity;
                    expPath.conductors.add(expConductor);

                    expBlockLink = reachedTileEntities.get(tileEntity);
                    if (expBlockLink != null) {
                        continue;
                    }
                    assert te != null;
                    IC2.platform.displayError("An exp network pathfinding entry is corrupted.\nThis could happen due to " +
                            "incorrect Minecraft behavior or a bug.\n\n(Technical information: expBlockLink, tile " +
                            "entities below)\nE: " + emitter + " (" + te.getX() + "," + te.getY() + "," + te

                            .getZ() + ")\n" + "C: " + tileEntity + " (" + te.getX() + "," + te

                            .getY() + "," + te

                            .getZ() + ")\n" + "R: " + expPath.target + " (" + this.expTileTileEntityMap
                            .get(expPath.target)
                            .getPos()
                            .getX() + "," + getTileFromMap(expPath.target).getPos().getY() + "," + getTileFromIEXP(
                            expPath.target).getPos().getZ() + ")");
                }
            }
        }
        return expPaths;
    }

    public IEXPTile getNeighbor(final IEXPTile tile, final EnumFacing dir) {
        if (tile == null) {
            return null;
        }
        if (!this.expTileTileEntityMap.containsKey(tile)) {
            return null;
        }
        return this.getTileEntity(this.expTileTileEntityMap.get(tile).getPos().offset(dir));
    }

    private List<EXPTarget> getValidReceivers(final IEXPTile emitter, final boolean reverse) {
        final List<EXPTarget> validReceivers = new LinkedList<>();

        for (final EnumFacing direction : EXPNetLocal.directions) {
            final IEXPTile target2 = getNeighbor(emitter, direction);
            if (target2 != null) {
                final EnumFacing inverseDirection2 = direction.getOpposite();
                if (reverse) {
                    if (emitter instanceof IEXPAcceptor && target2 instanceof IEXPEmitter) {
                        final IEXPEmitter sender2 = (IEXPEmitter) target2;
                        final IEXPAcceptor receiver2 = (IEXPAcceptor) emitter;
                        if (sender2.emitsEXPTo(receiver2, inverseDirection2) && receiver2.acceptsEXPFrom(
                                sender2,
                                direction
                        )) {
                            validReceivers.add(new EXPTarget(target2, inverseDirection2));
                        }
                    }
                } else if (emitter instanceof IEXPEmitter && target2 instanceof IEXPAcceptor) {
                    final IEXPEmitter sender2 = (IEXPEmitter) emitter;
                    final IEXPAcceptor receiver2 = (IEXPAcceptor) target2;
                    if (sender2.emitsEXPTo(receiver2, direction) && receiver2.acceptsEXPFrom(
                            sender2,
                            inverseDirection2
                    )) {
                        validReceivers.add(new EXPTarget(target2, inverseDirection2));
                    }
                }
            }
        }


        return validReceivers;
    }

    public List<IEXPSource> discoverFirstPathOrSources(final IEXPTile par1) {
        final Set<IEXPTile> reached = new HashSet<>();
        final List<IEXPSource> result = new ArrayList<>();
        final List<IEXPTile> workList = new ArrayList<>();
        workList.add(par1);
        while (workList.size() > 0) {
            final IEXPTile tile = workList.remove(0);

            final List<EXPTarget> targets = this.getValidReceivers(tile, true);
            for (EXPTarget expTarget : targets) {
                final IEXPTile target = expTarget.tileEntity;
                if (target != par1) {
                    if (!reached.contains(target)) {
                        reached.add(target);
                        if (target instanceof IEXPSource) {
                            result.add((IEXPSource) target);
                        } else if (target instanceof IEXPConductor) {
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
                final List<IEXPTile> tiles = this.waitingList.getPathTiles();
                for (final IEXPTile tile : tiles) {
                    final List<IEXPSource> sources = this.discoverFirstPathOrSources(tile);
                    if (sources.size() > 0) {
                        this.expSourceToEXPPathMap.removeAll(sources);
                    }
                }
                this.waitingList.clear();
            }
        }
        for (IEXPSource entry : this.sources) {
            if (entry != null) {
                final double offered = entry.getOfferedEXP();
                if (offered > 0) {
                    for (double packetAmount = 1, i = 0; i < packetAmount; ++i) {
                        final double removed = offered - this.emitEXPFrom(entry, offered);
                        if (removed <= 0) {
                            break;
                        }

                        entry.drawEXP(removed);
                    }
                }

            }
        }
    }

    public IEXPTile getTileEntity(BlockPos pos) {

        return this.chunkCoordinatesIEXPTileMap.get(pos);
    }


    void update(BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.values()) {
            BlockPos pos1 = pos
                    .offset(dir);
            final IEXPTile tile = this.chunkCoordinatesIEXPTileMap.get(pos1);
            if (tile != null) {
                if (tile instanceof IEXPConductor) {
                    ((IEXPConductor) tile).update_render();
                }
            }

        }
    }

    public void onUnload() {
        this.expSourceToEXPPathMap.clear();
        this.sources.clear();
        this.waitingList.clear();
        this.chunkCoordinatesIEXPTileMap.clear();
        this.chunkCoordinatesMap.clear();
        this.expTileTileEntityMap.clear();
    }

    static class EXPTarget {

        final IEXPTile tileEntity;
        final EnumFacing direction;

        EXPTarget(final IEXPTile tileEntity, final EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }

    }

    static class EXPPath {

        final List<IEXPConductor> conductors;
        final IEXPSink target;
        final EnumFacing targetDirection;
        long totalEXPConducted;

        EXPPath(IEXPSink sink, EnumFacing facing) {
            this.target = sink;
            this.conductors = new ArrayList<>();
            this.totalEXPConducted = 0L;
            this.targetDirection = facing;
        }

    }


    static class EXPPathMap {

        final Map<IEXPSource, List<EXPPath>> senderPath;

        EXPPathMap() {
            this.senderPath = new HashMap<>();
        }

        public void put(final IEXPSource par1, final List<EXPPath> par2) {
            this.senderPath.put(par1, par2);
        }


        public boolean containsKey(final IEXPSource par1) {
            return this.senderPath.containsKey(par1);
        }

        public List<EXPPath> get(final IEXPSource par1) {
            return this.senderPath.get(par1);
        }


        public void remove(final IEXPSource par1) {
            this.senderPath.remove(par1);
        }

        public void removeAll(final List<IEXPSource> par1) {
            if (par1 == null) {
                return;
            }
            for (IEXPSource iEXPSource : par1) {
                this.remove(iEXPSource);
            }
        }


        public List<IEXPSource> getSources(final IEXPAcceptor par1) {
            final List<IEXPSource> source = new ArrayList<>();
            for (final Map.Entry<IEXPSource, List<EXPPath>> entry : this.senderPath.entrySet()) {
                if (source.contains(entry.getKey())) {
                    continue;
                }
                for (EXPPath path : entry.getValue()) {
                    if ((!(par1 instanceof IEXPConductor) || !path.conductors.contains(par1)) && (!(par1 instanceof IEXPSink) || path.target != par1)) {
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

        final Set<IEXPTile> tiles;

        PathLogic() {
            this.tiles = new HashSet<>();
        }

        public boolean contains(final IEXPTile par1) {
            return this.tiles.contains(par1);
        }

        public void add(final IEXPTile par1) {
            this.tiles.add(par1);
        }

        public void remove(final IEXPTile par1) {
            this.tiles.remove(par1);
        }

        public void clear() {
            this.tiles.clear();
        }

        public IEXPTile getRepresentingTile() {
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

        public void onTileEntityAdded(final List<EXPTarget> around, final IEXPAcceptor tile) {
            if (around.isEmpty() || this.paths.isEmpty()) {
                this.createNewPath(tile);
                return;
            }
            boolean found = false;
            final List<PathLogic> logics = new ArrayList<>();
            for (final PathLogic logic : this.paths) {
                if (logic.contains(tile)) {
                    found = true;
                    if (tile instanceof IEXPConductor) {
                        logics.add(logic);
                    }
                } else {
                    for (final EXPTarget target : around) {
                        if (logic.contains(target.tileEntity)) {
                            found = true;
                            logic.add(tile);
                            if (target.tileEntity instanceof IEXPConductor) {
                                logics.add(logic);
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (logics.size() > 1 && tile instanceof IEXPConductor) {
                final PathLogic newLogic = new PathLogic();
                for (final PathLogic logic2 : logics) {
                    this.paths.remove(logic2);
                    for (final IEXPTile toMove : logic2.tiles) {
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

        public void onTileEntityRemoved(final IEXPAcceptor par1) {
            if (this.paths.isEmpty()) {
                return;
            }

            List<IEXPTile> toRecalculate = new ArrayList<>();
            for (int i = 0; i < this.paths.size(); i++) {
                PathLogic logic = this.paths.get(i);
                if (logic.contains(par1)) {
                    logic.remove(par1);
                    toRecalculate.addAll(logic.tiles);
                    this.paths.remove(i--);
                }
            }
            for (final IEXPTile tile : toRecalculate) {
                this.onTileEntityAdded(EXPNetLocal.this.getValidReceivers(tile, true), (IEXPAcceptor) tile);
            }
        }

        public void createNewPath(final IEXPTile par1) {
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

        public List<IEXPTile> getPathTiles() {
            final List<IEXPTile> tiles = new ArrayList<>();
            for (PathLogic path : this.paths) {
                final IEXPTile tile = path.getRepresentingTile();
                if (tile != null) {
                    tiles.add(tile);
                }
            }
            return tiles;
        }

    }

}
