package com.denfop.tiles.mechanism;

import com.denfop.items.ItemCable;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockFoam.FoamType;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.state.Ic2BlockState.Ic2BlockStateInstance;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.ref.TeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;


public class TileEntityCable extends TileEntityBlock implements IEnergyConductor, INetworkTileEntityEventListener {

    public static final float insulationThickness = 0.0625F;
    public static final IUnlistedProperty<TileEntityCable.CableRenderState> renderStateProperty = new UnlistedProperty(
            "renderstate",
            TileEntityCable.CableRenderState.class
    );
    protected CableType cableType;
    protected int insulation;

    private CableFoam foam;
    private final Obscuration obscuration;
    private byte connectivity;
    private volatile TileEntityCable.CableRenderState renderState;

    public boolean addedToEnergyNet;
    private IWorldTickCallback continuousUpdate;

    public static TileEntityCable delegate(CableType cableType, int insulation) {
        return new TileEntityCable(cableType, insulation);
    }

    public TileEntityCable(CableType cableType, int insulation) {
        this();
        this.cableType = cableType;
        this.insulation = insulation;
    }

    public TileEntityCable() {
        this.cableType = CableType.glass;
        this.foam = CableFoam.None;
        this.connectivity = 0;
        this.addedToEnergyNet = false;
        this.continuousUpdate = null;
        this.obscuration = this.addComponent(new Obscuration(
                this,
                () -> IC2.network.get(true).updateTileEntityField(TileEntityCable.this, "obscuration")
        ));
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.cableType = CableType.values[nbt.getByte("cableType") & 255];
        this.insulation = nbt.getByte("insulation") & 255;
        this.foam = CableFoam.values[nbt.getByte("foam") & 255];
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("cableType", (byte) this.cableType.ordinal());
        nbt.setByte("insulation", (byte) this.insulation);
        nbt.setByte("foam", (byte) this.foam.ordinal());
        return nbt;
    }

    protected void onLoaded() {
        super.onLoaded();
        if (this.getWorld().isRemote) {
            this.updateRenderState();
        } else {


            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
            this.updateConnectivity();
            if (this.foam == CableFoam.Soft) {
                this.changeFoam(this.foam, true);
            }
        }

    }

    protected void onUnloaded() {
        if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }

        if (this.continuousUpdate != null) {
            IC2.tickHandler.removeContinuousWorldTick(this.getWorld(), this.continuousUpdate);
            this.continuousUpdate = null;
        }

        super.onUnloaded();
    }

    protected SoundType getBlockSound(Entity entity) {
        return SoundType.CLOTH;
    }

    public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
        this.updateRenderState();
        super.onPlaced(stack, placer, facing);
    }

    protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
        return  ItemCable.getCable(this.cableType, this.insulation, 0);
    }

    protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
        if (this.foam == CableFoam.Hardened || this.foam == CableFoam.Soft && !forCollision) {
            return super.getAabbs(forCollision);
        } else {
            float th = this.cableType.thickness + (float) (this.insulation * 2) * 0.0625F;
            float sp = (1.0F - th) / 2.0F;
            List<AxisAlignedBB> ret = new ArrayList(7);
            ret.add(new AxisAlignedBB(
                    sp,
                    sp,
                    sp,
                    sp + th,
                    sp + th,
                    sp + th
            ));
            EnumFacing[] var5 = EnumFacing.VALUES;
            int var6 = var5.length;

            for (EnumFacing facing : var5) {
                boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0;
                if (hasConnection) {
                    float zS = sp;
                    float yS = sp;
                    float xS = sp;
                    float yE;
                    float zE;
                    float xE = yE = zE = sp + th;
                    switch (facing) {
                        case DOWN:
                            yS = 0.0F;
                            yE = sp;
                            break;
                        case UP:
                            yS = sp + th;
                            yE = 1.0F;
                            break;
                        case NORTH:
                            zS = 0.0F;
                            zE = sp;
                            break;
                        case SOUTH:
                            zS = sp + th;
                            zE = 1.0F;
                            break;
                        case WEST:
                            xS = 0.0F;
                            xE = sp;
                            break;
                        case EAST:
                            xS = sp + th;
                            xE = 1.0F;
                            break;
                        default:
                            throw new RuntimeException();
                    }

                    ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
                }
            }

            return ret;
        }
    }

    @SideOnly(Side.CLIENT)
    protected boolean shouldSideBeRendered(EnumFacing side, BlockPos otherPos) {
        return false;
    }

    protected boolean isNormalCube() {
        return false;
    }

    protected boolean doesSideBlockRendering(EnumFacing side) {
        return false;
    }

    protected boolean isSideSolid(EnumFacing side) {
        return false;
    }

    protected boolean clientNeedsExtraModelInfo() {
        return true;
    }

    public boolean shouldRenderInPass(int pass) {
        return true;
    }

    public Ic2BlockStateInstance getExtendedState(Ic2BlockStateInstance state) {
        state = super.getExtendedState(state);
        TileEntityCable.CableRenderState cableRenderState = this.renderState;
        if (cableRenderState != null) {
            state = state.withProperties(renderStateProperty, cableRenderState);
        }


        return state;
    }

    public void onNeighborChange(Block neighbor, BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        if (!this.getWorld().isRemote) {
            this.updateConnectivity();
        }

    }

    private void updateConnectivity() {
        World world = this.getWorld();
        byte newConnectivity = 0;
        int mask = 1;
        EnumFacing[] var4 = EnumFacing.VALUES;
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            EnumFacing dir = var4[var6];
            IEnergyTile tile = EnergyNet.instance.getSubTile(world, this.pos.offset(dir));
            if ((tile instanceof IEnergyAcceptor && ((IEnergyAcceptor) tile).acceptsEnergyFrom(
                    this,
                    dir.getOpposite()
            ) || tile instanceof IEnergyEmitter && ((IEnergyEmitter) tile).emitsEnergyTo(
                    this,
                    dir.getOpposite()
            )) && this.canInteractWith(tile, dir)) {
                newConnectivity = (byte) (newConnectivity | mask);
            }

            mask *= 2;
        }

        if (this.connectivity != newConnectivity) {
            this.connectivity = newConnectivity;
            IC2.network.get(true).updateTileEntityField(this, "connectivity");
        }

    }

    protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

        return super.onActivated(player, hand, side, hitX, hitY, hitZ);

    }

    protected void onClicked(EntityPlayer player) {
        super.onClicked(player);


    }

    protected float getHardness() {
        return super.getHardness();
    }

    protected float getExplosionResistance(Entity exploder, Explosion explosion) {

        return super.getHardness();

    }

    protected int getLightOpacity() {
        return this.foam == CableFoam.Hardened ? 255 : 0;
    }


    protected boolean recolor(EnumFacing side, EnumDyeColor mcColor) {
        return false;
    }

    protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest) {
        return !this.changeFoam(CableFoam.None, false) && super.onRemovedByPlayer(player, willHarvest);
    }


    public boolean tryRemoveInsulation(boolean simulate) {
        if (this.insulation <= 0) {
            return false;
        } else if (simulate) {
            return true;
        } else {
            if (this.insulation == this.cableType.minColoredInsulation) {
                CableFoam foam = this.foam;
                this.foam = CableFoam.None;
                this.recolor(this.getFacing(), EnumDyeColor.BLACK);
                this.foam = foam;
            }

            --this.insulation;
            if (!this.getWorld().isRemote) {
                IC2.network.get(true).updateTileEntityField(this, "insulation");
            }

            return true;
        }
    }

    public boolean wrenchCanRemove(EntityPlayer player) {
        return false;
    }

    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
        return this.canInteractWith(emitter, direction);
    }

    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
        return this.canInteractWith(receiver, direction);
    }

    public boolean canInteractWith(IEnergyTile tile, EnumFacing side) {

        return true;
    }

    public double getConductionLoss() {
        return this.cableType.loss;
    }

    public double getInsulationEnergyAbsorption() {
        if (this.cableType.maxInsulation == 0) {
            return 2.147483647E9D;
        } else {
            return this.cableType == CableType.glass
                    ? EnergyNet.instance.getPowerFromTier(this.insulation)
                    : EnergyNet.instance.getPowerFromTier(this.insulation + 1);
        }
    }

    public double getInsulationBreakdownEnergy() {
        return 9001.0D;
    }

    public double getConductorBreakdownEnergy() {
        return this.cableType.capacity + 1;
    }

    public void removeInsulation() {
        this.tryRemoveInsulation(false);
    }

    public void removeConductor() {
        this.getWorld().setBlockToAir(this.pos);
        IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
    }


    public List<String> getNetworkedFields() {
        List<String> ret = new ArrayList();
        ret.add("cableType");
        ret.add("insulation");
        ret.add("foam");
        ret.add("connectivity");
        ret.add("obscuration");
        ret.addAll(super.getNetworkedFields());
        return ret;
    }

    public void onNetworkUpdate(String field) {
        this.updateRenderState();
        if (field.equals("foam") && (this.foam == CableFoam.None || this.foam == CableFoam.Hardened)) {
            this.relight();
        }

        this.rerender();
        super.onNetworkUpdate(field);
    }

    private void relight() {
    }

    public void onNetworkEvent(int event) {
        World world = this.getWorld();
        if (event == 0) {
            world.playSound(
                    null,
                    this.pos,
                    SoundEvents.ENTITY_GENERIC_BURN,
                    SoundCategory.BLOCKS,
                    0.5F,
                    2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
            );

            for (int l = 0; l < 8; ++l) {
                world.spawnParticle(
                        EnumParticleTypes.SMOKE_LARGE,
                        (double) this.pos.getX() + Math.random(),
                        (double) this.pos.getY() + 1.2D,
                        (double) this.pos.getZ() + Math.random(),
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

        } else {
            IC2.platform.displayError(
                    "An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.pos + ")"
            );
        }
    }

    private boolean changeFoam(CableFoam foam, boolean duringLoad) {
        if (this.foam == foam && !duringLoad) {
            return false;
        } else {
            World world = this.getWorld();
            if (!world.isRemote) {
                this.foam = foam;
                if (this.continuousUpdate != null) {
                    IC2.tickHandler.removeContinuousWorldTick(world, this.continuousUpdate);
                    this.continuousUpdate = null;
                }

                if (foam != CableFoam.Hardened) {
                    this.obscuration.clear();

                }

                if (foam == CableFoam.Soft) {
                    this.continuousUpdate = new IWorldTickCallback() {
                        public void onTick(World world) {
                            if (world.rand.nextFloat() < BlockFoam.getHardenChance(
                                    world,
                                    TileEntityCable.this.pos,
                                    TileEntityCable.this.getBlockType().getState(TeBlock.cable),
                                    FoamType.normal
                            )) {
                                TileEntityCable.this.changeFoam(CableFoam.Hardened, false);
                            }

                        }
                    };
                    IC2.tickHandler.requestContinuousWorldTick(world, this.continuousUpdate);
                }

                if (!duringLoad) {
                    IC2.network.get(true).updateTileEntityField(this, "foam");
                    world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), true);
                    this.markDirty();
                }

            }
            return true;
        }
    }


    private void updateRenderState() {
        this.renderState = new TileEntityCable.CableRenderState(
                this.cableType,
                this.insulation,
                this.foam,
                this.connectivity,
                this.getActive()
        );
    }

    public static class CableRenderState {

        public final CableType type;
        public final int insulation;
        public final CableFoam foam;
        public final int connectivity;
        public final boolean active;

        public CableRenderState(CableType type, int insulation, CableFoam foam, int connectivity, boolean active) {
            this.type = type;
            this.insulation = insulation;
            this.foam = foam;
            this.connectivity = connectivity;
            this.active = active;
        }

        public int hashCode() {
            int ret = this.type.hashCode();
            ret = ret * 31 + this.insulation;
            ret = ret * 31 + this.foam.hashCode();
            ret = ret * 31 + this.connectivity;
            ret = ret << 1 | (this.active ? 1 : 0);
            return ret;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof TileEntityCable.CableRenderState)) {
                return false;
            } else {
                TileEntityCable.CableRenderState o = (TileEntityCable.CableRenderState) obj;
                return o.type == this.type && o.insulation == this.insulation && o.foam == this.foam && o.connectivity == this.connectivity && o.active == this.active;
            }
        }

        public String toString() {
            return "CableState<" + this.type + ", " + this.insulation + ", " + this.foam + ", " + this.connectivity + ", " + this.active + '>';
        }

    }

}
