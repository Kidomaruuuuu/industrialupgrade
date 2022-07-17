package com.denfop.tiles.mechanism.blastfurnace.block;

import com.denfop.tiles.mechanism.blastfurnace.api.BlastSystem;
import com.denfop.tiles.mechanism.blastfurnace.api.IBlastMain;
import com.denfop.tiles.mechanism.blastfurnace.api.IOtherBlastPart;
import ic2.core.block.TileEntityBlock;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

public class TileEntityOtherPart extends TileEntityBlock implements IOtherBlastPart {

    public IBlastMain blastMain;

    @Override
    public IBlastMain getMain() {
        return blastMain;
    }

    @Override
    public void setMain(final IBlastMain blastMain) {
        this.blastMain = blastMain;
    }

    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        BlastSystem.instance.update(this.pos,world,this);
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
    protected void onLoaded() {
        super.onLoaded();
        BlastSystem.instance.update(this.pos,world,this);
    }

    @Override
    protected void onUnloaded() {
        if(this.getMain() != null)
            if(this.getMain().getFull())
                this.getMain().setFull(false);
        super.onUnloaded();

    }

}
