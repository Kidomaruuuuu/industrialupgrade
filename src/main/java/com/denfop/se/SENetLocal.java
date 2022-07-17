package com.denfop.se;

import com.denfop.api.se.ISEAcceptor;
import com.denfop.api.se.ISEConductor;
import com.denfop.api.se.ISEEmitter;
import com.denfop.api.se.ISESink;
import com.denfop.api.se.ISESource;
import com.denfop.api.se.ISETile;
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

public class SENetLocal {

    private static EnumFacing[] directions;

    static {
        SENetLocal.directions = EnumFacing.values();
    }

    private final World world;
    private final SEPathMap seSourceToSEPathMap;
    private final Map<ISETile, BlockPos> chunkCoordinatesMap;
    private final Map<ISETile, TileEntity> seTileTileEntityMap;

    private final Map<BlockPos, ISETile> chunkCoordinatesISETileMap;
    private final List<ISESource> sources;
    private final WaitingList waitingList;
    private int tick;

    SENetLocal(final World world) {
        this.seSourceToSEPathMap = new SEPathMap();
        this.sources = new ArrayList<>();
        this.waitingList = new WaitingList();
        this.world = world;
        this.chunkCoordinatesISETileMap = new HashMap<>();
        this.chunkCoordinatesMap = new HashMap<>();
        this.seTileTileEntityMap = new HashMap<>();
        this.tick = 0;
    }

    public void addTile(ISETile tile1) {


        this.addTileEntity(getTileFromISE(tile1).getPos(), tile1);


    }

    public BlockPos getPos(final ISETile tile) {
        return this.chunkCoordinatesMap.get(tile);
    }

    public void addTileEntity(final BlockPos coords, final ISETile tile) {
        if (this.chunkCoordinatesISETileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromISE(tile);
        this.seTileTileEntityMap.put(tile, te);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesISETileMap.put(coords, tile);
        this.update(coords);
        if (tile instanceof ISEAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), (ISEAcceptor) tile);
        }
        if (tile instanceof ISESource) {
            this.sources.add((ISESource) tile);
        }
    }

    public void removeTile(ISETile tile1) {

        this.removeTileEntity(tile1);

    }

    public void removeTileEntity(BlockPos coord, ISETile tile, ISETile tile1) {
        if (!this.chunkCoordinatesISETileMap.containsKey(coord)) {
            return;
        }
        this.chunkCoordinatesMap.remove(tile, coord);

        this.chunkCoordinatesISETileMap.remove(coord);
        this.seTileTileEntityMap.remove(tile1, this.seTileTileEntityMap.get(tile1));
        this.seTileTileEntityMap.remove(tile, this.seTileTileEntityMap.get(tile));
        this.update(coord);
        if (tile instanceof ISEAcceptor) {
            this.seSourceToSEPathMap.removeAll(this.seSourceToSEPathMap.getSources((ISEAcceptor) tile));
            this.waitingList.onTileEntityRemoved((ISEAcceptor) tile);
        }
        if (tile instanceof ISESource) {
            this.sources.remove((ISESource) tile);
            this.seSourceToSEPathMap.remove((ISESource) tile);
        }
    }

    public void removeTileEntity(ISETile tile) {
        if (!this.seTileTileEntityMap.containsKey(tile)) {
            return;
        }
        final BlockPos coord = this.chunkCoordinatesMap.get(tile);
        this.chunkCoordinatesMap.remove(tile);
        this.seTileTileEntityMap.remove(tile, this.seTileTileEntityMap.get(tile));
        this.chunkCoordinatesISETileMap.remove(coord, tile);
        this.update(coord);
        if (tile instanceof ISEAcceptor) {
            this.seSourceToSEPathMap.removeAll(this.seSourceToSEPathMap.getSources((ISEAcceptor) tile));
            this.waitingList.onTileEntityRemoved((ISEAcceptor) tile);
        }
        if (tile instanceof ISESource) {
            this.sources.remove((ISESource) tile);
            this.seSourceToSEPathMap.remove((ISESource) tile);
        }
    }

    public TileEntity getTileFromMap(ISETile tile) {
        return this.seTileTileEntityMap.get(tile);
    }

    public double emitSEFrom(final ISESource seSource, double amount) {
        List<SEPath> sePaths = this.seSourceToSEPathMap.get(seSource);
        if (sePaths == null) {
            sePaths = this.discover(seSource);
            this.seSourceToSEPathMap.put(seSource, sePaths);
        }
        if (amount > 0) {
            for (final SEPath sePath : sePaths) {
                if (amount <= 0) {
                    break;
                }
                final ISESink seSink = sePath.target;
                double demandedSE = seSink.getDemandedSE();
                if (demandedSE <= 0.0) {
                    continue;
                }
                double seProvided = Math.floor(Math.round(amount));
                double adding = Math.min(seProvided, demandedSE);
                if (adding <= 0.0D) {
                    continue;
                }
                seSink.injectSE(sePath.targetDirection, adding, 0);
                sePath.totalSEConducted = (long) adding;

                amount -= adding;
                amount = Math.max(0, amount);


            }
        }

        return amount;
    }


    public TileEntity getTileFromISE(ISETile tile) {
        if (tile instanceof TileEntity) {
            return (TileEntity) tile;
        }
        if (tile instanceof ILocatable) {
            return this.world.getTileEntity(((ILocatable) tile).getPosition());
        }

        return null;
    }

    private List<SEPath> discover(final ISESource emitter) {
        final Map<ISEConductor, EnumFacing> reachedTileEntities = new HashMap<>();
        final List<ISETile> tileEntitiesToCheck = new ArrayList<>();
        final List<SEPath> sePaths = new ArrayList<>();

        tileEntitiesToCheck.add(emitter);

        while (!tileEntitiesToCheck.isEmpty()) {
            final ISETile currentTileEntity = tileEntitiesToCheck.remove(0);
            final List<SETarget> validReceivers = this.getValidReceivers(currentTileEntity, false);
            for (final SETarget validReceiver : validReceivers) {
                if (validReceiver.tileEntity != emitter) {
                    if (validReceiver.tileEntity instanceof ISESink) {
                        sePaths.add(new SEPath((ISESink) validReceiver.tileEntity, validReceiver.direction));
                        continue;
                    }
                    if (reachedTileEntities.containsKey((ISEConductor) validReceiver.tileEntity)) {
                        continue;
                    }

                    reachedTileEntities.put((ISEConductor) validReceiver.tileEntity, validReceiver.direction);
                    tileEntitiesToCheck.add(validReceiver.tileEntity);
                }
            }


        }
        for (SEPath sePath : sePaths) {
            ISETile tileEntity = sePath.target;
            EnumFacing seBlockLink = sePath.targetDirection;
            if (emitter != null) {
                while (tileEntity != emitter) {
                    BlockPos te = this.chunkCoordinatesMap.get(tileEntity);
                    if (seBlockLink != null && te != null) {
                        tileEntity = this.getTileEntity(te.offset(seBlockLink));
                    }
                    if (!(tileEntity instanceof ISEConductor)) {
                        break;
                    }
                    final ISEConductor seConductor = (ISEConductor) tileEntity;
                    sePath.conductors.add(seConductor);

                    seBlockLink = reachedTileEntities.get(tileEntity);
                    if (seBlockLink != null) {
                        continue;
                    }
                    assert te != null;
                    IC2.platform.displayError("An se network pathfinding entry is corrupted.\nThis could happen due to " +
                            "incorrect Minecraft behavior or a bug.\n\n(Technical information: seBlockLink, tile " +
                            "entities below)\nE: " + emitter + " (" + te.getX() + "," + te.getY() + "," + te

                            .getZ() + ")\n" + "C: " + tileEntity + " (" + te.getX() + "," + te

                            .getY() + "," + te

                            .getZ() + ")\n" + "R: " + sePath.target + " (" + this.seTileTileEntityMap
                            .get(sePath.target)
                            .getPos()
                            .getX() + "," + getTileFromMap(sePath.target).getPos().getY() + "," + getTileFromISE(
                            sePath.target).getPos().getZ() + ")");
                }
            }
        }
        return sePaths;
    }

    public ISETile getNeighbor(final ISETile tile, final EnumFacing dir) {
        if (tile == null) {
            return null;
        }
        if (!this.seTileTileEntityMap.containsKey(tile)) {
            return null;
        }
        return this.getTileEntity(this.seTileTileEntityMap.get(tile).getPos().offset(dir));
    }

    private List<SETarget> getValidReceivers(final ISETile emitter, final boolean reverse) {
        final List<SETarget> validReceivers = new LinkedList<>();

        for (final EnumFacing direction : SENetLocal.directions) {
            final ISETile target2 = getNeighbor(emitter, direction);
            if (target2 != null) {
                final EnumFacing inverseDirection2 = direction.getOpposite();
                if (reverse) {
                    if (emitter instanceof ISEAcceptor && target2 instanceof ISEEmitter) {
                        final ISEEmitter sender2 = (ISEEmitter) target2;
                        final ISEAcceptor receiver2 = (ISEAcceptor) emitter;
                        if (sender2.emitsSETo(receiver2, inverseDirection2) && receiver2.acceptsSEFrom(
                                sender2,
                                direction
                        )) {
                            validReceivers.add(new SETarget(target2, inverseDirection2));
                        }
                    }
                } else if (emitter instanceof ISEEmitter && target2 instanceof ISEAcceptor) {
                    final ISEEmitter sender2 = (ISEEmitter) emitter;
                    final ISEAcceptor receiver2 = (ISEAcceptor) target2;
                    if (sender2.emitsSETo(receiver2, direction) && receiver2.acceptsSEFrom(
                            sender2,
                            inverseDirection2
                    )) {
                        validReceivers.add(new SETarget(target2, inverseDirection2));
                    }
                }
            }
        }


        return validReceivers;
    }

    public List<ISESource> discoverFirstPathOrSources(final ISETile par1) {
        final Set<ISETile> reached = new HashSet<>();
        final List<ISESource> result = new ArrayList<>();
        final List<ISETile> workList = new ArrayList<>();
        workList.add(par1);
        while (workList.size() > 0) {
            final ISETile tile = workList.remove(0);

            final List<SETarget> targets = this.getValidReceivers(tile, true);
            for (SETarget seTarget : targets) {
                final ISETile target = seTarget.tileEntity;
                if (target != par1) {
                    if (!reached.contains(target)) {
                        reached.add(target);
                        if (target instanceof ISESource) {
                            result.add((ISESource) target);
                        } else if (target instanceof ISEConductor) {
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
                final List<ISETile> tiles = this.waitingList.getPathTiles();
                for (final ISETile tile : tiles) {
                    final List<ISESource> sources = this.discoverFirstPathOrSources(tile);
                    if (sources.size() > 0) {
                        this.seSourceToSEPathMap.removeAll(sources);
                    }
                }
                this.waitingList.clear();
            }
        }
        for (ISESource entry : this.sources) {
            if (entry != null) {
                final double offered = entry.getOfferedSE();
                if (offered > 0) {
                    for (double packetAmount = 1, i = 0; i < packetAmount; ++i) {
                        final double removed = offered - this.emitSEFrom(entry, offered);
                        if (removed <= 0) {
                            break;
                        }

                        entry.drawSE(removed);
                    }
                }

            }
        }
        this.tick++;
    }

    public ISETile getTileEntity(BlockPos pos) {

        return this.chunkCoordinatesISETileMap.get(pos);
    }


    void update(BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.values()) {
            BlockPos pos1 = pos
                    .offset(dir);
            final ISETile tile = this.chunkCoordinatesISETileMap.get(pos1);
            if (tile != null) {
                if (tile instanceof ISEConductor) {
                    ((ISEConductor) tile).update_render();
                }
            }

        }
    }

    public void onUnload() {
        this.seSourceToSEPathMap.clear();
        this.sources.clear();
        this.waitingList.clear();
        this.chunkCoordinatesISETileMap.clear();
        this.chunkCoordinatesMap.clear();
        this.seTileTileEntityMap.clear();
    }

    static class SETarget {

        final ISETile tileEntity;
        final EnumFacing direction;

        SETarget(final ISETile tileEntity, final EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }

    }

    static class SEPath {

        final List<ISEConductor> conductors;
        final ISESink target;
        final EnumFacing targetDirection;
        long totalSEConducted;

        SEPath(ISESink sink, EnumFacing facing) {
            this.target = sink;
            this.conductors = new ArrayList<>();
            this.totalSEConducted = 0L;
            this.targetDirection = facing;
        }

    }


    static class SEPathMap {

        final Map<ISESource, List<SEPath>> senderPath;

        SEPathMap() {
            this.senderPath = new HashMap<>();
        }

        public void put(final ISESource par1, final List<SEPath> par2) {
            this.senderPath.put(par1, par2);
        }


        public boolean containsKey(final ISESource par1) {
            return this.senderPath.containsKey(par1);
        }

        public List<SEPath> get(final ISESource par1) {
            return this.senderPath.get(par1);
        }


        public void remove(final ISESource par1) {
            this.senderPath.remove(par1);
        }

        public void removeAll(final List<ISESource> par1) {
            if (par1 == null) {
                return;
            }
            for (ISESource iSESource : par1) {
                this.remove(iSESource);
            }
        }


        public List<ISESource> getSources(final ISEAcceptor par1) {
            final List<ISESource> source = new ArrayList<>();
            for (final Map.Entry<ISESource, List<SEPath>> entry : this.senderPath.entrySet()) {
                if (source.contains(entry.getKey())) {
                    continue;
                }
                for (SEPath path : entry.getValue()) {
                    if ((!(par1 instanceof ISEConductor) || !path.conductors.contains(par1)) && (!(par1 instanceof ISESink) || path.target != par1)) {
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

        final Set<ISETile> tiles;

        PathLogic() {
            this.tiles = new HashSet<>();
        }

        public boolean contains(final ISETile par1) {
            return this.tiles.contains(par1);
        }

        public void add(final ISETile par1) {
            this.tiles.add(par1);
        }

        public void remove(final ISETile par1) {
            this.tiles.remove(par1);
        }

        public void clear() {
            this.tiles.clear();
        }

        public ISETile getRepresentingTile() {
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

        public void onTileEntityAdded(final List<SETarget> around, final ISEAcceptor tile) {
            if (around.isEmpty() || this.paths.isEmpty()) {
                this.createNewPath(tile);
                return;
            }
            boolean found = false;
            final List<PathLogic> logics = new ArrayList<>();
            for (final PathLogic logic : this.paths) {
                if (logic.contains(tile)) {
                    found = true;
                    if (tile instanceof ISEConductor) {
                        logics.add(logic);
                    }
                } else {
                    for (final SETarget target : around) {
                        if (logic.contains(target.tileEntity)) {
                            found = true;
                            logic.add(tile);
                            if (target.tileEntity instanceof ISEConductor) {
                                logics.add(logic);
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (logics.size() > 1 && tile instanceof ISEConductor) {
                final PathLogic newLogic = new PathLogic();
                for (final PathLogic logic2 : logics) {
                    this.paths.remove(logic2);
                    for (final ISETile toMove : logic2.tiles) {
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

        public void onTileEntityRemoved(final ISEAcceptor par1) {
            if (this.paths.isEmpty()) {
                return;
            }

            List<ISETile> toRecalculate = new ArrayList<>();
            for (int i = 0; i < this.paths.size(); i++) {
                PathLogic logic = this.paths.get(i);
                if (logic.contains(par1)) {
                    logic.remove(par1);
                    toRecalculate.addAll(logic.tiles);
                    this.paths.remove(i--);
                }
            }
            for (final ISETile tile : toRecalculate) {
                this.onTileEntityAdded(SENetLocal.this.getValidReceivers(tile, true), (ISEAcceptor) tile);
            }
        }

        public void createNewPath(final ISETile par1) {
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

        public List<ISETile> getPathTiles() {
            final List<ISETile> tiles = new ArrayList<>();
            for (PathLogic path : this.paths) {
                final ISETile tile = path.getRepresentingTile();
                if (tile != null) {
                    tiles.add(tile);
                }
            }
            return tiles;
        }

    }

}
