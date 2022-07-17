package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.container.ContainerBlastFurnace;
import com.denfop.container.ContainerPlasticPlateCreator;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiBlastFurnace extends GuiIC2<ContainerBlastFurnace> {

    public final ContainerBlastFurnace container;

    public GuiBlastFurnace(ContainerBlastFurnace container1) {
        super(container1);
        this.container = container1;
    }

    protected void drawForegroundLayer(int par1, int par2) {
        super.drawForegroundLayer(par1, par2);
        if( container.base.tank1 != null)
        TankGauge.createNormal(this, 6, 5, container.base.tank1).drawForeground(par1, par2);
        TankGauge.createNormal(this, 27, 5, container.base.tank).drawForeground(par1, par2);
        if(this.container.base.component != null) {
            String temp = (int) this.container.base.component.storage + "°C" + "/" + (int) this.container.base.component.getCapacity() + "°C";
            new AdvArea(this, 67, 60, 97, 70).withTooltip(temp).drawForeground(par1, par2);
        }
        String temp =
                ModUtils.getString( this.container.base.bar * 100000D) + " Pa";
        new AdvArea(this, 136, 63, 164, 66).withTooltip(temp).drawForeground(par1, par2);
        new AdvArea(this, 136, 69, 146, 79).withTooltip("+1").drawForeground(par1, par2);
        new AdvArea(this, 154, 69, 164, 79).withTooltip("-1").drawForeground(par1, par2);
        new AdvArea(this, 80, 35, 101, 49).withTooltip(Localization.translate("gui.MolecularTransformer.progress")+": " + (int)(Math.min(this.container.base.getProgress() / 3600D, 1D) * 100) + "%").drawForeground(par1, par2);


    }
    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
        int xMin = (this.width - this.xSize) / 2;
        int yMin = (this.height - this.ySize) / 2;
        int x = i - xMin;
        int y = j - yMin;
        if (x >= 136 && x <= 146 && y >= 69 && y <= 79) {
            IC2.network.get(false).initiateClientTileEntityEvent(this.container.base, 0);
        }
        if (x >= 154 && x <= 164 && y >= 69 && y <= 79) {
            IC2.network.get(false).initiateClientTileEntityEvent(this.container.base, 1);
        }

    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.mc.getTextureManager().bindTexture(getTexture());
        int progress = (int) (24.0F * this.container.base.getProgress() / 3600);
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (progress > 0) {
            drawTexturedModalRect(xoffset + 79, yoffset + 34, 176, 14, progress + 1, 16);
        }
        if( container.base.tank1 != null)
        TankGauge.createNormal(this, 6, 5,  container.base.tank1).drawBackground(xoffset, yoffset);
        TankGauge.createNormal(this, 27, 5,  container.base.tank).drawBackground(xoffset, yoffset);
        this.mc.getTextureManager().bindTexture(getTexture());
        if(this.container.base.component != null) {
            int temperature;

            temperature =
                    (int) (30 * this.container.base.component.getEnergy() / this.container.base.component.getCapacity());
            temperature = Math.min(30,temperature);
            if (temperature > 0) {
                drawTexturedModalRect(this.guiLeft + 67, this.guiTop + 60, 177, 39, temperature + 1, 11);
            }
        }
        int bar = (int) ((Math.min(this.container.base.bar * 1D,5D)/5D) * 27D);
        if (bar > 0) {
            drawTexturedModalRect(this.guiLeft + 136, this.guiTop + 63, 177, 104, bar + 1, 3);
        }

      }



    public ResourceLocation getTexture() {
        return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiblastfurnace.png");
    }

}
