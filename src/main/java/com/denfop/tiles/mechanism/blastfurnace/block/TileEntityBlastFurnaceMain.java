package com.denfop.tiles.mechanism.blastfurnace.block;

import com.denfop.Ic2Items;
import com.denfop.componets.HeatComponent;
import com.denfop.container.ContainerBlastFurnace;
import com.denfop.gui.GuiBlastFurnace;
import com.denfop.tiles.mechanism.blastfurnace.api.BlastSystem;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastHeat;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastInputFluid;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastInputItem;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastMain;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastOutputItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.ref.FluidName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityBlastFurnaceMain extends TileEntityInventory implements IBlastMain, INetworkClientTileEntityEventListener,
        IHasGui {

    public final FluidTank tank;
    public FluidTank tank1 = null;
    public HeatComponent component = null;
    private boolean full;
    public IBlastHeat blastHeat;
    public IBlastInputFluid blastInputFluid;
    public IBlastInputItem blastInputItem;
    public IBlastOutputItem blastOutputItem;
    public List<EntityPlayer> entityPlayerList;
    public double progress = 0;
    public int bar = 1;

    public TileEntityBlastFurnaceMain() {
        this.full = false;
        final Fluids fluids = this.addComponent(new Fluids(this));
        this.tank = fluids.addTank("tank", 10000, InvSlot.Access.I, InvSlot.InvSide.ANY,
                Fluids.fluidPredicate(FluidName.steam.getInstance())
        );
        this.entityPlayerList = new ArrayList<>();
    }

    @Override
    public boolean getFull() {
        return full;
    }

    @Override
    public void update_block() {
        full = BlastSystem.instance.getFull(this.getFacing(), this.pos, this.getWorld(), this);
    }

    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        update_block();
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        update_block();
    }

    @Override
    protected void onUnloaded() {
        if (this.full) {
            BlastSystem.instance.deleteMain(this.getFacing(), this.pos, this.getWorld(), this);
        }
        super.onUnloaded();

    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (!this.full) {
            if (this.getActive()) {
                this.setActive(false);
            }
            return;
        }
        try {

            if (!this.getInputItem().getInput().isEmpty()) {
                int amount_stream = tank.getFluidAmount();
                if (this.getHeat().getHeatComponent().getEnergy() == this.getHeat().getHeatComponent().getCapacity()) {
                    int bar1 = bar;
                    if (amount_stream < bar1 * 2) {
                        bar1 = amount_stream / 2;
                    }
                    if (bar1 > 0) {
                        if (progress == 0) {
                            this.setActive(true);
                        }
                        if(!this.getActive())
                            this.setActive(true);
                        progress += 1 + (0.25 * (bar1 - 1));
                        tank.drain(Math.min(bar1 * 2, this.tank.getFluidAmount()), true);
                        if (progress >= 3600 && this.getOutputItem().getOutput().add(Ic2Items.advIronIngot)) {
                            progress = 0;
                            this.getInputItem().getInput().get(0).shrink(1);
                            this.setActive(false);
                        }
                    }
                }
                double heat = this.getHeat().getHeatComponent().getEnergy();
                if (heat > 250 && this.tank.getFluidAmount() + 2 < this.tank.getCapacity()) {
                    int size = this.tank1.getFluidAmount();
                    int size_stream = this.tank.getCapacity() - this.tank.getFluidAmount();
                    int size1 = size / 5;
                    size1 = Math.min(size1, 10);
                    if (size1 > 0) {
                        int add = Math.min(size_stream / 2, size1);
                        if (add > 0) {
                            this.tank.fill(new FluidStack(FluidName.steam.getInstance(), add * 2), true);
                            this.getInputFluid().getFluidTank().drain(add * 5, true);

                        }
                    }
                }

            } else if (this.getActive()) {
                this.setActive(false);
            }
                if(this.component.getEnergy() > 0)
                    this.component.useEnergy(1);
        } catch (Exception ignored) {
        }
    }

    @Override
    public IBlastHeat getHeat() {
        return blastHeat;
    }

    @Override
    public IBlastInputFluid getInputFluid() {
        return blastInputFluid;
    }

    @Override
    public IBlastInputItem getInputItem() {
        return blastInputItem;
    }

    @Override
    public IBlastOutputItem getOutputItem() {
        return blastOutputItem;
    }

    @Override
    public void setHeat(final IBlastHeat blastHeat) {
        this.blastHeat = blastHeat;
        if (this.blastHeat == null) {
            this.component = null;
        } else {
            this.component = this.blastHeat.getHeatComponent();
        }
    }

    @Override
    public void setInputFluid(final IBlastInputFluid blastInputFluid) {
        this.blastInputFluid = blastInputFluid;
        if (this.blastInputFluid == null) {
            this.tank1 = null;
        } else {
            this.tank1 = this.blastInputFluid.getFluidTank();
        }
    }

    @Override
    public void setInputItem(final IBlastInputItem blastInputItem) {
        this.blastInputItem = blastInputItem;
    }

    @Override
    public void setOutputItem(final IBlastOutputItem blastOutputItem) {
        this.blastOutputItem = blastOutputItem;
    }

    @Override
    public void setFull(final boolean full) {
        if (!full) {
            if (!this.entityPlayerList.isEmpty()) {
                this.entityPlayerList.forEach(EntityPlayer::closeScreen);
            }
        }
        this.full = full;
    }

    @Override
    public double getProgress() {
        return this.progress;
    }

    @Override
    public void onNetworkEvent(final EntityPlayer entityPlayer, final int i) {
        switch (i) {
            case 0:
                this.bar = Math.min(this.bar + 1, 5);
                break;
            case 1:
                this.bar = Math.max(1, this.bar - 1);
                break;
        }
    }

    @Override
    public boolean onActivated(
            final EntityPlayer player,
            final EnumHand hand,
            final EnumFacing side,
            final float hitX,
            final float hitY,
            final float hitZ
    ) {
        if (!this.full) {
            return false;
        }

        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public ContainerBlastFurnace getGuiContainer(final EntityPlayer entityPlayer) {
        if (!this.entityPlayerList.contains(entityPlayer)) {
            this.entityPlayerList.add(entityPlayer);
        }
        return new ContainerBlastFurnace(entityPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiBlastFurnace getGui(final EntityPlayer entityPlayer, final boolean b) {

        return new GuiBlastFurnace(this.getGuiContainer(entityPlayer));
    }

    @Override
    public void onGuiClosed(final EntityPlayer entityPlayer) {

    }

}
