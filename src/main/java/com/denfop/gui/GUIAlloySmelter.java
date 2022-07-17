package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.container.ContainerDoubleElectricMachine;
import com.denfop.tiles.mechanism.dual.heat.TileEntityAlloySmelter;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAlloySmelter extends GuiIC2<ContainerDoubleElectricMachine> {

    public final ContainerDoubleElectricMachine container;

    public GuiAlloySmelter(ContainerDoubleElectricMachine container1) {
        super(container1);
        this.container = container1;
    }

    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        new AdvArea(this, 104, 58, 142, 69)
                .withTooltip(Localization.translate("iu.temperature") + ModUtils.getString(((TileEntityAlloySmelter) this.container.base).heat.storage) + "/" + ModUtils.getString(
                        ((TileEntityAlloySmelter) this.container.base).heat.capacity) + "°C")
                .drawForeground(mouseX, mouseY);
        String tooltip2 =
                ModUtils.getString(Math.min(
                        this.container.base.energy.getEnergy(),
                        this.container.base.energy.getCapacity()
                )) + "/" + ModUtils.getString(this.container.base.energy.getCapacity()) + " " +
                        "EU";
        new AdvArea(this, 58, 35, 69, 50)
                .withTooltip(tooltip2)
                .drawForeground(mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(getTexture());
        int chargeLevel = (int) (14.0F * this.container.base.getChargeLevel());
        int progress = (int) (24.0F * this.container.base.getProgress());
        if (chargeLevel > 0) {
            drawTexturedModalRect(xoffset + 56 + 1, yoffset + 36 + 14 - chargeLevel, 176, 14 - chargeLevel,
                    14, chargeLevel
            );
        }
        if (progress > 0) {
            drawTexturedModalRect(xoffset + 79, yoffset + 34, 176, 14, progress + 1, 16);
        }
        int temperature =
                (int) (38 * ((TileEntityAlloySmelter) this.container.base).heat.storage / ((TileEntityAlloySmelter) this.container.base).heat.capacity);
        if (temperature > 0) {
            drawTexturedModalRect(this.guiLeft + 104, this.guiTop + 58, 176, 35, temperature + 1, 11);
        }


    }

    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/GuiAlloySmelter.png");
    }


}
