package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.container.ContainerHandlerHeavyOre;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHandlerHeavyOre extends GuiIC2<ContainerHandlerHeavyOre> {

    public final ContainerHandlerHeavyOre container;

    public GuiHandlerHeavyOre(ContainerHandlerHeavyOre container1) {
        super(container1);
        this.container = container1;
    }

    protected void drawForegroundLayer(int par1, int par2) {
        super.drawForegroundLayer(par1, par2);
        new AdvArea(this, 51, 52, 89, 63)
                .withTooltip(Localization.translate("iu.temperature") + ModUtils.getString(this.container.base.heat.storage) + "/" + ModUtils.getString(
                        this.container.base.heat.capacity) + "°C")
                .drawForeground(par1, par2);


        String tooltip2 =
                ModUtils.getString(Math.min(
                        this.container.base.energy.getEnergy(),
                        this.container.base.energy.getCapacity()
                )) + "/" + ModUtils.getString(this.container.base.energy.getCapacity()) + " " +
                        "EU";
        new AdvArea(this, 26, 56, 37, 71)
                .withTooltip(tooltip2)
                .drawForeground(par1, par2);

    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.bindTexture();

        int progress = (int) (44 * this.container.base.getProgress());

        if (progress > 0) {
            drawTexturedModalRect(this.guiLeft + 48, this.guiTop + 31, 177, 32, progress + 1, 14);
        }
        int temperature = (int) (38 * this.container.base.heat.storage / this.container.base.heat.capacity);
        if (temperature > 0) {
            drawTexturedModalRect(this.guiLeft + 51, this.guiTop + 52, 176, 50, temperature + 1, 11);
        }
        int chargeLevel = (int) (14.0F * this.container.base.getChargeLevel());
        if (chargeLevel > 0) {
            drawTexturedModalRect(this.guiLeft + 25, this.guiTop + 57 + 14 - chargeLevel, 176, 14 - chargeLevel,
                    14, chargeLevel
            );
        }


    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/GUIHandlerHO.png");
    }

}
