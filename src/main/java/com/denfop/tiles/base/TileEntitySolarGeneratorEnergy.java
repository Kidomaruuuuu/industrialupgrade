package com.denfop.tiles.base;

import com.denfop.IUItem;
import com.denfop.api.recipe.InvSlotOutput;
import com.denfop.componets.SEComponent;
import com.denfop.container.ContainerSolarGeneratorEnergy;
import com.denfop.gui.GuiSolarGeneratorEnergy;
import com.denfop.invslot.InvSlotGenSunarrium;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.init.Localization;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class TileEntitySolarGeneratorEnergy extends TileEntityInventory implements IHasGui,
        INetworkClientTileEntityEventListener {

    public final InvSlotGenSunarrium input;
    public final InvSlotOutput outputSlot;
    public final ItemStack itemstack = new ItemStack(IUItem.sunnarium, 1, 4);
    public final double maxSunEnergy;
    public final double cof;
    public boolean work;
    public SEComponent sunenergy;
    public List<Double> lst;
    private boolean noSunWorld;
    private boolean skyIsVisible;
    private boolean sunIsUp;

    public TileEntitySolarGeneratorEnergy(double cof) {

        this.maxSunEnergy = 4500;
        this.cof = cof;
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.input = new InvSlotGenSunarrium(this);
        this.lst = new ArrayList<>();
        this.lst.add(0D);
        this.lst.add(0D);
        this.lst.add(0D);
        this.sunenergy = this.addComponent(SEComponent
                .asBasicSource(this, 10000, 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("press.lshift"));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("iu.solarium_energy.info"));
        }
        super.addInformation(stack, tooltip, advanced);

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

    public List<String> getNetworkedFields() {
        List<String> ret = super.getNetworkedFields();
        ret.add("sunenergy");

        return ret;

    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.lst = this.input.coefday();
        this.noSunWorld = this.world.provider.isNether();
        updateVisibility();

    }

    public void updateVisibility() {
        this.skyIsVisible = this.world.canBlockSeeSky(this.pos.up()) &&
                (this.world.getBlockState(this.pos.up()).getMaterial().getMaterialMapColor() ==
                        MapColor.AIR) && !this.noSunWorld;
        this.sunIsUp = this.world.isDaytime();
    }

    public void updateEntityServer() {

        super.updateEntityServer();
        if (this.world.provider.getWorldTime() % 80 == 0) {
            updateVisibility();
        }
        long tick = this.getWorld().provider.getWorldTime() % 24000L;
        if (this.skyIsVisible) {
            energy(tick);
            if (this.sunenergy.getEnergy() >= 4500) {
                if (this.outputSlot.get().stackSize < 64 || this.outputSlot.isEmpty()) {
                    if (this.outputSlot.add(itemstack)) {
                        this.sunenergy.addEnergy(-4500);
                    }
                }
            }
        }

    }

    public void energy(long tick) {
        double k = 0;
        if (this.sunIsUp) {
            if (tick <= 1000L) {
                k = 5;
            }
            if (tick > 1000L && tick <= 4000L) {
                k = 10;
            }
            if (tick > 4000L && tick <= 8000L) {
                k = 30;
            }
            if (tick > 8000L && tick <= 11000L) {
                k = 10;
            }
            if (tick > 11000L) {
                k = 5;
            }
            this.sunenergy.addEnergy(k * this.cof * (1 + lst.get(0)));
        }

        if (lst.get(2) > 0 && !this.sunIsUp) {
            double tick1 = tick - 12000;
            if (tick1 <= 1000L) {
                k = 5;
            }
            if (tick1 > 1000L && tick1 <= 4000L) {
                k = 10;
            }
            if (tick1 > 4000L && tick1 <= 8000L) {
                k = 30;
            }
            if (tick1 > 8000L && tick1 <= 11000L) {
                k = 10;
            }
            if (tick1 > 11000L) {
                k = 5;
            }
            this.sunenergy.addEnergy(k * this.cof * (lst.get(2) - 1) * (1 + lst.get(1)));

        }

    }


    @Override
    public void onGuiClosed(EntityPlayer arg0) {
    }


    public ContainerBase<? extends TileEntitySolarGeneratorEnergy> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerSolarGeneratorEnergy(entityPlayer, this);
    }


    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiSolarGeneratorEnergy(new ContainerSolarGeneratorEnergy(entityPlayer, this));
    }

    @Override
    public void onNetworkEvent(final EntityPlayer entityPlayer, final int i) {
        this.work = !this.work;
    }

}
