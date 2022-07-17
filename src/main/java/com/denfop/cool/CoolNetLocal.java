package com.denfop.cool;

import com.denfop.api.cool.ICoolAcceptor;
import com.denfop.api.cool.ICoolConductor;
import com.denfop.api.cool.ICoolEmitter;
import com.denfop.api.cool.ICoolSink;
import com.denfop.api.cool.ICoolSource;
import com.denfop.api.cool.ICoolTile;
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

public class CoolNetLocal {

    private static EnumFacing[] directions;

    static {
        CoolNetLocal.directions = EnumFacing.values();
    }

    private final World world;
    private final CoolPathMap coolSourceToCoolPathMap;
    private final Map<ICoolTile, BlockPos> chunkCoordinatesMap;
    private final Map<ICoolTile, TileEntity> coolTileTileEntityMap;

    private final Map<BlockPos, ICoolTile> chunkCoordinatesICoolTileMap;
    private final List<ICoolSource> sources;
    private final WaitingList waitingList;
    private int tick;

    CoolNetLocal(final World world) {
        this.coolSourceToCoolPathMap = new CoolPathMap();
        this.sources = new ArrayList<>();
        this.waitingList = new WaitingList();
        this.world = world;
        this.chunkCoordinatesICoolTileMap = new HashMap<>();
        this.chunkCoordinatesMap = new HashMap<>();
        this.coolTileTileEntityMap = new HashMap<>();
        this.tick = 0;
    }

    public void addTile(ICoolTile tile1) {


        this.addTileEntity(getTileFromICool(tile1).getPos(), tile1);


    }

    public BlockPos getPos(final ICoolTile tile) {
        return this.chunkCoordinatesMap.get(tile);
    }

    public void addTileEntity(final BlockPos coords, final ICoolTile tile) {
        if (this.chunkCoordinatesICoolTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromICool(tile);
        this.coolTileTileEntityMap.put(tile, te);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesICoolTileMap.put(coords, tile);
        this.update(coords);
        if (tile instanceof ICoolAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), (ICoolAcceptor) tile);
        }
        if (tile instanceof ICoolSource) {
            this.sources.add((ICoolSource) tile);
        }
    }

    public void addTileEntity(final BlockPos coords, final ICoolTile tile, final ICoolTile tile1) {
        if (this.chunkCoordinatesICoolTileMap.containsKey(coords)) {
            return;
        }

        TileEntity te = getTileFromICool(tile);
        TileEntity te1 = getTileFromICool(tile1);
        this.coolTileTileEntityMap.put(tile, te);
        this.coolTileTileEntityMap.put(tile1, te1);
        this.chunkCoordinatesMap.put(tile, coords);
        this.chunkCoordinatesICoolTileMap.put(coords, tile);

        this.update(coords);
        if (tile instanceof ICoolAcceptor) {
            this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), (ICoolAcceptor) tile);
        }
        if (tile instanceof ICoolSource) {
            this.sources.add((ICoolSource) tile);

        }


    }

    public void removeTile(ICoolTile tile1) {

        this.removeTileEntity(tile1);

    }

    public void removeTileEntity(BlockPos coord, ICoolTile tile, ICoolTile tile1) {
        if (!this.chunkCoordinatesICoolTileMap.containsKey(coord)) {
            return;
        }
        this.chunkCoordinatesMap.remove(tile, coord);

        this.chunkCoordinatesICoolTileMap.remove(coord);
        this.coolTileTileEntityMap.remove(tile1, this.coolTileTileEntityMap.get(tile1));
        this.coolTileTileEntityMap.remove(tile, this.coolTileTileEntityMap.get(tile));
        this.update(coord);
        if (tile instanceof ICoolAcceptor) {
            this.coolSourceToCoolPathMap.removeAll(this.coolSourceToCoolPathMap.getSources((ICoolAcceptor) tile));
            this.waitingList.onTileEntityRemoved((ICoolAcceptor) tile);
        }
        if (tile instanceof ICoolSource) {
            this.sources.remove((ICoolSource) tile);
            this.coolSourceToCoolPathMap.remove((ICoolSource) tile);
        }
    }

    public void removeTileEntity(ICoolTile tile) {
        if (!this.coolTileTileEntityMap.containsKey(tile)) {
            return;
        }
        final BlockPos coord = this.chunkCoordinatesMap.get(tile);
        this.chunkCoordinatesMap.remove(tile);
        this.coolTileTileEntityMap.remove(tile, this.coolTileTileEntityMap.get(tile));
        this.chunkCoordinatesICoolTileMap.remove(coord, tile);
        this.update(coord);
        if (tile instanceof ICoolAcceptor) {
            this.coolSourceToCoolPathMap.removeAll(this.coolSourceToCoolPathMap.getSources((ICoolAcceptor) tile));
            this.waitingList.onTileEntityRemoved((ICoolAcceptor) tile);
        }
        if (tile instanceof ICoolSource) {
            this.sources.remove((ICoolSource) tile);
            this.coolSourceToCoolPathMap.remove((ICoolSource) tile);
        }
    }

    public TileEntity getTileFromMap(ICoolTile tile) {
        return this.coolTileTileEntityMap.get(tile);
    }

    public double emitCoolFrom(final ICoolSource coolSource, double amount, double offered) {
        List<CoolPath> coolPaths = this.coolSourceToCoolPathMap.get(coolSource);
        if (coolPaths == null) {
            coolPaths = this.discover(coolSource);
            this.coolSourceToCoolPathMap.put(coolSource, coolPaths);
        }
        if (amount > 0) {
            for (final CoolPath coolPath : coolPaths) {
                final ICoolSink coolSink = coolPath.target;
                double demandedCool = coolSink.getDemandedCool();
                if (demandedCool <= 0.0) {
                    continue;
                }
                double coolProvided = Math.floor(Math.round(amount));
                double adding = Math.min(coolProvided, demandedCool);
                if (adding <= 0.0D) {
                    continue;
                }
                coolSink.injectCool(coolPath.targetDirection, adding, 0);
                coolPath.totalCoolConducted = (long) adding;

                for (ICoolConductor energyConductor3 : coolPath.conductors) {
                    if (coolSource.getOfferedCool() >= energyConductor3.getConductorBreakdownEnergy()) {
                        energyConductor3.removeConductor();
                    }
                }

            }
        }

        return amount;
    }


    public TileEntity getTileFromICool(ICoolTile tile) {
        if (tile instanceof TileEntity) {
            return (TileEntity) tile;
        }
        if (tile instanceof ILocatable) {
            return this.world.getTileEntity(((ILocatable) tile).getPosition());
        }

        return null;
    }

    private List<CoolPath> discover(final ICoolSource emitter) {
        final Map<ICoolConductor, EnumFacing> reachedTileEntities = new HashMap<>();
        final List<ICoolTile> tileEntitiesToCheck = new ArrayList<>();
        final List<CoolPath> coolPaths = new ArrayList<>();

        tileEntitiesToCheck.add(emitter);

        while (!tileEntitiesToCheck.isEmpty()) {
            final ICoolTile currentTileEntity = tileEntitiesToCheck.remove(0);
            final List<CoolTarget> validReceivers = this.getValidReceivers(currentTileEntity, false);
            for (final CoolTarget validReceiver : validReceivers) {
                if (validReceiver.tileEntity != emitter) {
                    if (validReceiver.tileEntity instanceof ICoolSink) {
                        coolPaths.add(new CoolPath((ICoolSink) validReceiver.tileEntity, validReceiver.direction));
                        continue;
                    }
                    if (reachedTileEntities.containsKey((ICoolConductor) validReceiver.tileEntity)) {
                        continue;
                    }

                    reachedTileEntities.put((ICoolConductor) validReceiver.tileEntity, validReceiver.direction);
                    tileEntitiesToCheck.add(validReceiver.tileEntity);
                }
            }


        }
        for (CoolPath coolPath : coolPaths) {
            ICoolTile tileEntity = coolPath.target;
            EnumFacing coolBlockLink = coolPath.targetDirection;
            if (emitter != null) {
                while (tileEntity != emitter) {
                    BlockPos te = this.chunkCoordinatesMap.get(tileEntity);
                    if (coolBlockLink != null && te != null) {
                        tileEntity = this.getTileEntity(te.offset(coolBlockLink));
                    }
                    if (!(tileEntity instanceof ICoolConductor)) {
                        break;
                    }
                    final ICoolConductor coolConductor = (ICoolConductor) tileEntity;
                    coolPath.conductors.add(coolConductor);

                    coolBlockLink = reachedTileEntities.get(tileEntity);
                    if (coolBlockLink != null) {
                        continue;
                    }
                    assert te != null;
                    IC2.platform.displayError("An cool network pathfinding entry is corrupted.\nThis could happen due to " +
                            "incorrect Minecraft behavior or a bug.\n\n(Technical information: coolBlockLink, tile " +
                            "entities below)\nE: " + emitter + " (" + te.getX() + "," + te.getY() + "," + te

                            .getZ() + ")\n" + "C: " + tileEntity + " (" + te.getX() + "," + te

                            .getY() + "," + te

                            .getZ() + ")\n" + "R: " + coolPath.target + " (" + this.coolTileTileEntityMap
                            .get(coolPath.target)
                            .getPos()
                            .getX() + "," + getTileFromMap(coolPath.target).getPos().getY() + "," + getTileFromICool(
                            coolPath.target).getPos().getZ() + ")");
                }
            }
        }
        return coolPaths;
    }

    public ICoolTile getNeighbor(final ICoolTile tile, final EnumFacing dir) {
        if (tile == null) {
            return null;
        }
        if (!this.coolTileTileEntityMap.containsKey(tile)) {
            return null;
        }
        return this.getTileEntity(this.coolTileTileEntityMap.get(tile).getPos().offset(dir));
    }

    public ICoolTile getNeighbor(final ICoolTile tile, final EnumFacing dir, List<ICoolTile> tiles) {
        if (tile == null) {
            return null;
        }
        if (!this.coolTileTileEntityMap.containsKey(tile)) {
            return null;
        }
        ICoolTile tile1 = this.getTileEntity(this.coolTileTileEntityMap.get(tile).getPos().offset(dir));
        if (tiles.contains(tile1)) {
            return null;
        }
        return tile1;
    }

    private List<CoolTarget> getValidReceivers(final ICoolTile emitter, final boolean reverse) {
        final List<CoolTarget> validReceivers = new LinkedList<>();

        for (final EnumFacing direction : CoolNetLocal.directions) {
            final ICoolTile target2 = getNeighbor(emitter, direction);
            if (target2 != null) {
                final EnumFacing inverseDirection2 = direction.getOpposite();
                if (reverse) {
                    if (emitter instanceof ICoolAcceptor && target2 instanceof ICoolEmitter) {
                        final ICoolEmitter sender2 = (ICoolEmitter) target2;
                        final ICoolAcceptor receiver2 = (ICoolAcceptor) emitter;
                        if (sender2.emitsCoolTo(receiver2, inverseDirection2) && receiver2.acceptsCoolFrom(
                                sender2,
                                direction
                        )) {
                            validReceivers.add(new CoolTarget(target2, inverseDirection2));
                        }
                    }
                } else if (emitter instanceof ICoolEmitter && target2 instanceof ICoolAcceptor) {
                    final ICoolEmitter sender2 = (ICoolEmitter) emitter;
                    final ICoolAcceptor receiver2 = (ICoolAcceptor) target2;
                    if (sender2.emitsCoolTo(receiver2, direction) && receiver2.acceptsCoolFrom(
                            sender2,
                            inverseDirection2
                    )) {
                        validReceivers.add(new CoolTarget(target2, inverseDirection2));
                    }
                }
            }
        }


        return validReceivers;
    }

    public List<ICoolSource> discoverFirstPathOrSources(final ICoolTile par1) {
        final Set<ICoolTile> reached = new HashSet<>();
        final List<ICoolSource> result = new ArrayList<>();
        final List<ICoolTile> workList = new ArrayList<>();
        workList.add(par1);
        while (workList.size() > 0) {
            final ICoolTile tile = workList.remove(0);

            final List<CoolTarget> targets = this.getValidReceivers(tile, true);
            for (CoolTarget coolTarget : targets) {
                final ICoolTile target = coolTarget.tileEntity;
                if (target != par1) {
                    if (!reached.contains(target)) {
                        reached.add(target);
                        if (target instanceof ICoolSource) {
                            result.add((ICoolSource) target);
                        } else if (target instanceof ICoolConductor) {
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
                final List<ICoolTile> tiles = this.waitingList.getPathTiles();
                for (final ICoolTile tile : tiles) {
                    final List<ICoolSource> sources = this.discoverFirstPathOrSources(tile);
                    if (sources.size() > 0) {
                        this.coolSourceToCoolPathMap.removeAll(sources);
                    }
                }
                this.waitingList.clear();
            }
        }
        for (ICoolSource entry : this.sources) {
            if (entry != null) {
                final double offered = entry.getOfferedCool();
                if (offered > 0) {
                    for (double packetAmount = 1, i = 0; i < packetAmount; ++i) {
                        final double removed = offered - this.emitCoolFrom(entry, offered, offered);
                        if (removed <= 0) {
                            break;
                        }

                        entry.drawCool(removed);
                    }
                }

            }
        }
        this.tick++;
    }

    public ICoolTile getTileEntity(BlockPos pos) {

        return this.chunkCoordinatesICoolTileMap.get(pos);
    }


    void update(BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.values()) {
            BlockPos pos1 = pos
                    .offset(dir);
            final ICoolTile tile = this.chunkCoordinatesICoolTileMap.get(pos1);
            if (tile != null) {
                if (tile instanceof ICoolConductor) {
                    ((ICoolConductor) tile).update_render();
                }
            }

        }
    }

    public void onUnload() {
        this.coolSourceToCoolPathMap.clear();
        this.sources.clear();
        this.waitingList.clear();
        this.chunkCoordinatesICoolTileMap.clear();
        this.chunkCoordinatesMap.clear();
        this.coolTileTileEntityMap.clear();
    }

    static class CoolTarget {

        final ICoolTile tileEntity;
        final EnumFacing direction;

        CoolTarget(final ICoolTile tileEntity, final EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }

    }

    static class CoolPath {

        final Set<ICoolConductor> conductors;
        final ICoolSink target;
        final EnumFacing targetDirection;
        long totalCoolConducted;

        CoolPath(ICoolSink sink, EnumFacing facing) {
            this.target = sink;
            this.conductors = new HashSet<>();
            this.totalCoolConducted = 0L;
            this.targetDirection = facing;
        }

    }


    static class CoolPathMap {

        final Map<ICoolSource, List<CoolPath>> senderPath;

        CoolPathMap() {
            this.senderPath = new HashMap<>();
        }

        public void put(final ICoolSource par1, final List<CoolPath> par2) {
            this.senderPath.put(par1, par2);
        }


        public boolean containsKey(final ICoolSource par1) {
            return this.senderPath.containsKey(par1);
        }

        public List<CoolPath> get(final ICoolSource par1) {
            return this.senderPath.get(par1);
        }


        public void remove(final ICoolSource par1) {
            this.senderPath.remove(par1);
        }

        public void removeAll(final List<ICoolSource> par1) {
            if (par1 == null) {
                return;
            }
            for (ICoolSource iCoolSource : par1) {
                this.remove(iCoolSource);
            }
        }


        public List<ICoolSource> getSources(final ICoolAcceptor par1) {
            final List<ICoolSource> source = new ArrayList<>();
            for (final Map.Entry<ICoolSource, List<CoolPath>> entry : this.senderPath.entrySet()) {
                if (source.contains(entry.getKey())) {
                    continue;
                }
                for (CoolPath path : entry.getValue()) {
                    if ((!(par1 instanceof ICoolConductor) || !path.conductors.contains(par1)) && (!(par1 instanceof ICoolSink) || path.target != par1)) {
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

        final Set<ICoolTile> tiles;

        PathLogic() {
            this.tiles = new HashSet<>();
        }

        public boolean contains(final ICoolTile par1) {
            return this.tiles.contains(par1);
        }

        public void add(final ICoolTile par1) {
            this.tiles.add(par1);
        }

        public void remove(final ICoolTile par1) {
            this.tiles.remove(par1);
        }

        public void clear() {
            this.tiles.clear();
        }

        public ICoolTile getRepresentingTile() {
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

        public void onTileEntityAdded(final List<CoolTarget> around, final ICoolAcceptor tile) {
            if (around.isEmpty() || this.paths.isEmpty()) {
                this.createNewPath(tile);
                return;
            }
            boolean found = false;
            final List<PathLogic> logics = new ArrayList<>();
            for (final PathLogic logic : this.paths) {
                if (logic.contains(tile)) {
                    found = true;
                    if (tile instanceof ICoolConductor) {
                        logics.add(logic);
                    }
                } else {
                    for (final CoolTarget target : around) {
                        if (logic.contains(target.tileEntity)) {
                            found = true;
                            logic.add(tile);
                            if (target.tileEntity instanceof ICoolConductor) {
                                logics.add(logic);
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (logics.size() > 1 && tile instanceof ICoolConductor) {
                final PathLogic newLogic = new PathLogic();
                for (final PathLogic logic2 : logics) {
                    this.paths.remove(logic2);
                    for (final ICoolTile toMove : logic2.tiles) {
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

        public void onTileEntityRemoved(final ICoolAcceptor par1) {
            if (this.paths.isEmpty()) {
                return;
            }

            List<ICoolTile> toRecalculate = new ArrayList<>();
            for (int i = 0; i < this.paths.size(); i++) {
                PathLogic logic = this.paths.get(i);
                if (logic.contains(par1)) {
                    logic.remove(par1);
                    toRecalculate.addAll(logic.tiles);
                    this.paths.remove(i--);
                }
            }
            for (final ICoolTile tile : toRecalculate) {
                this.onTileEntityAdded(CoolNetLocal.this.getValidReceivers(tile, true), (ICoolAcceptor) tile);
            }
        }

        public void createNewPath(final ICoolTile par1) {
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

        public List<ICoolTile> getPathTiles() {
            final List<ICoolTile> tiles = new ArrayList<>();
            for (PathLogic path : this.paths) {
                final ICoolTile tile = path.getRepresentingTile();
                if (tile != null) {
                    tiles.add(tile);
                }
            }
            return tiles;
        }

    }

}
