package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.container.ContainerDoubleElectricMachine;
import com.denfop.utils.ModUtils;
import ic2.api.recipe.RecipeOutput;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GUISynthesis extends GuiIC2<ContainerDoubleElectricMachine> {

    public final ContainerDoubleElectricMachine container;

    public GUISynthesis(ContainerDoubleElectricMachine container1) {
        super(container1);
        this.container = container1;
    }

    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        String tooltip2 =
                ModUtils.getString(Math.min(
                        this.container.base.energy.getEnergy(),
                        this.container.base.energy.getCapacity()
                )) + "/" + ModUtils.getString(this.container.base.energy.getCapacity()) + " " +
                        "EU";
        new AdvArea(this, 25, 55, 36, 70)
                .withTooltip(tooltip2)
                .drawForeground(mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.mc.getTextureManager().bindTexture(getTexture());
        int chargeLevel = (int) (14.0F * this.container.base.getChargeLevel());
        int progress = (int) (10 * this.container.base.getProgress());
        int progress1 = (int) (24 * this.container.base.getProgress());
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;

        if (chargeLevel > 0) {
            drawTexturedModalRect(xoffset + 24, yoffset + 56 + 14 - chargeLevel, 176, 14 - chargeLevel,
                    14, chargeLevel
            );
        }
        if (progress > 0) {
            drawTexturedModalRect(xoffset + 39, yoffset + 37, 177, 35, progress + 1, 9);
        }
        if (progress1 > 0) {
            drawTexturedModalRect(xoffset + 82, yoffset + 30, 177, 52, progress1 + 1, 23);
        }

        final BaseMachineRecipe output = this.container.base.getOutput();
        if (output != null) {
            this.fontRenderer.drawString(
                    TextFormatting.GREEN + Localization.translate("chance") + output.output.metadata.getInteger(
                            "percent") + "%", xoffset + 69,
                    yoffset + 67, ModUtils.convertRGBcolorToInt(217, 217, 217)
            );
        }
    }

    public String getName() {
        return this.container.base.getInventoryName();
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/GUISynthesis.png");
    }

}
