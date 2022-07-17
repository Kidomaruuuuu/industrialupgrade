package com.denfop.tiles.mechanism.blastfurnace.block;

import com.denfop.api.recipe.InvSlotOutput;
import com.denfop.tiles.mechanism.blastfurnace.api.BlastSystem;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastInputFluid;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastMain;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.mutable.MutableObject;

public class TileEntityFluidInput extends TileEntityInventory implements IBlastInputFluid {

    private final InvSlotConsumableLiquidByList fluidSlot;
    IBlastMain blastMain;
    FluidTank tank;
    InvSlotOutput output;

    public TileEntityFluidInput(){
        final Fluids fluids = this.addComponent(new Fluids(this));
        this.tank = fluids.addTank("tank",10000, InvSlot.Access.I, InvSlot.InvSide.ANY,
                Fluids.fluidPredicate( FluidRegistry.WATER));
        this.fluidSlot = new InvSlotConsumableLiquidByList(this, "fluidSlot", 1, FluidRegistry.WATER);
        output = new InvSlotOutput(this,"output1",1);
    }

    @Override
    public IBlastMain getMain() {
        return blastMain;
    }

    @Override
    public void setMain(final IBlastMain blastMain) {
        this.blastMain = blastMain;
    }
    @Override
    protected boolean onActivated(
            final EntityPlayer player,
            final EnumHand hand,
            final EnumFacing side,
            final float hitX,
            final float hitY,
            final float hitZ
    ) {
        if(this.getMain() != null)
            return ((TileEntityBlastFurnaceMain)this.getMain()).onActivated(player, hand, side, hitX, hitY, hitZ);
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        MutableObject<ItemStack> output1 = new MutableObject<>();
        if (this.fluidSlot.transferToTank(
                this.tank,
                output1,
                true
        ) && (output1.getValue() == null || this.output.canAdd(output1.getValue()))) {
            this.fluidSlot.transferToTank(this.tank, output1, false);
            if (output1.getValue() != null) {
                this.output.add(output1.getValue());
            }
        }
    }

    @Override
    public FluidTank getFluidTank() {
        return tank;
    }

    @Override
    public InvSlotOutput getInvSlotOutput() {
        return this.output;
    }

    @Override
    public InvSlotConsumableLiquidByList getInvSlotConsumableLiquidBy() {
        return this.fluidSlot;
    }

    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        BlastSystem.instance.update(this.pos,world,this);
    }
    @Override
    protected void onUnloaded() {
        if(this.getMain() != null)
            if(this.getMain().getFull()) {
                this.getMain().setFull(false);
                this.getMain().setInputFluid(null);
            }
        super.onUnloaded();

    }
    @Override
    protected void onLoaded() {
        super.onLoaded();
        BlastSystem.instance.update(this.pos,world,this);
    }
}
