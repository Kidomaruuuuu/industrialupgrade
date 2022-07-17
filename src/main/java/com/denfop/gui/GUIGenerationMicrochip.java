package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.container.ContainerBaseGenerationChipMachine;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiGenerationMicrochip extends GuiIC2<ContainerBaseGenerationChipMachine> {

    public final ContainerBaseGenerationChipMachine container;

    public GuiGenerationMicrochip(
            ContainerBaseGenerationChipMachine container1
    ) {
        super(container1);
        this.container = container1;
    }

    @Override
    protected void drawForegroundLayer(int par1, int par2) {
        super.drawForegroundLayer(par1, par2);
        new AdvArea(this, 70, 62, 108, 73)
                .withTooltip(Localization.translate("iu.temperature") + ModUtils.getString(this.container.base.heat.getEnergy()) + "/" + ModUtils.getString(
                        this.container.base.heat.getCapacity()) + "°C")
                .drawForeground(par1, par2);

        String tooltip2 =
                ModUtils.getString(Math.min(
                        this.container.base.energy.getEnergy(),
                        this.container.base.energy.getCapacity()
                )) + "/" + ModUtils.getString(this.container.base.energy.getCapacity()) + " " +
                        "EU";
        new AdvArea(this, 7, 62, 18, 77)
                .withTooltip(tooltip2)
                .drawForeground(par1, par2);
    }


    protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        String name = Localization.translate(this.container.base.getName());
        this.drawXCenteredString(this.xSize / 2, 0, name, 4210752, false);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(getTexture());
        int chargeLevel = (int) (14.0F * this.container.base.getChargeLevel());
        int progress = (int) (15.0F * this.container.base.getProgress());
        int progress1 = (int) (10.0F * this.container.base.getProgress());
        int progress2 = (int) (19.0F * this.container.base.getProgress());
        int temperature = (int) (38 * this.container.base.heat.storage / this.container.base.heat.capacity);
        if (chargeLevel > 0) {
            drawTexturedModalRect(xoffset + 6, yoffset + 76 - 13 + 14 - chargeLevel, 176, 14 - chargeLevel,
                    14, chargeLevel
            );
        }
        if (progress > 0) {
            drawTexturedModalRect(xoffset + 27, yoffset + 13, 176, 34, progress + 1, 28);
        }
        if (progress1 > 0) {
            drawTexturedModalRect(xoffset + 60, yoffset + 17, 176, 64, progress1 + 1, 19);
        }
        if (progress2 > 0) {
            drawTexturedModalRect(xoffset + 88, yoffset + 23, 176, 85, progress2 + 1, 7);
        }


        if (temperature > 0) {
            drawTexturedModalRect(xoffset + 70, yoffset + 62, 176, 20, temperature + 1, 11);
        }
    }

    public String getName() {
        return "";
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(Constants.TEXTURES, "textures/gui/GUICirsuit.png");
    }

}
