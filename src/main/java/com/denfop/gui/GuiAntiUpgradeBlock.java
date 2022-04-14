package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.api.upgrade.UpgradeSystem;
import com.denfop.container.ContainerAntiUpgrade;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.init.Localization;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.util.List;

public class GuiAntiUpgradeBlock extends GuiIC2<ContainerAntiUpgrade> {

    public final ContainerAntiUpgrade container;

    public GuiAntiUpgradeBlock(ContainerAntiUpgrade container1) {
        super(container1);
        this.container = container1;
    }

    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
        int xMin = (this.width - this.xSize) / 2;
        int yMin = (this.height - this.ySize) / 2;
        int x = i - xMin;
        int y = j - yMin;
        for (int m = 0; m < 4; m++) {
            if (x >= 70 && x <= 87 && y >= 10 + 18 * m && y < 27 + 18 * m) {
                IC2.network.get(false).initiateClientTileEntityEvent(this.container.base, m + 1);
            }
        }
    }

    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(0, (this.width - this.xSize) / 2 + 12, (this.height - this.ySize) / 2 + 15,
                50, 20, Localization.translate("button.need_mod")
        ));

    }

    protected void actionPerformed(GuiButton guibutton) {

        if (guibutton.id == 0) {
            IC2.network.get(false).initiateClientTileEntityEvent(this.container.base, 0);
        }


    }

    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/guiantiupgrade.png");
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
        new AdvArea(this, 26, 56, 37, 71)
                .withTooltip(tooltip2)
                .drawForeground(mouseX, mouseY);
    }

    protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        String name = Localization.translate(this.container.base.getName());
        this.drawXCenteredString(this.xSize / 2, 1, name, 4210752, false);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);

        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(getTexture());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        final int progress = Math.min(29 * this.container.base.progress / 100, 29);

        int chargeLevel = (int) (14.0F * this.container.base.getChargeLevel());
        if (chargeLevel > 0) {
            drawTexturedModalRect(xoffset + 25, yoffset + 57 + 14 - chargeLevel, 176, 14 - chargeLevel,
                    14, chargeLevel
            );

        }
        if (progress > 0) {
            drawTexturedModalRect(
                    xoffset + 136,
                    yoffset + 56 - progress,
                    176,
                    49 - progress + 28,
                    4,
                    progress
            );
        }
        if (!this.container.base.input.isEmpty()) {
            final List<ItemStack> list = UpgradeSystem.system.getListStack(this.container.base.input.get());
            int i = 0;
            GL11.glColor4f(1F, 1, 1F, 1);
            drawTexturedModalRect(xoffset + 70, yoffset + 10 + 18 * this.container.base.index, 200,
                    10 + 18 * this.container.base.index, 18, 18
            );

            for (ItemStack stack : list) {

                if (stack.isEmpty()) {
                    i++;
                    continue;
                }
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glPushMatrix();
                GL11.glColor4f(1F, 1, 1F, 1);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GlStateManager.disableLighting();
                GlStateManager.enableDepth();
                this.zLevel = 100.0F;
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                itemRender.renderItemAndEffectIntoGUI(
                        stack,
                        xoffset + 71,
                        yoffset + 11 + i * 18
                );
                GL11.glEnable(GL11.GL_LIGHTING);
                GlStateManager.enableLighting();

                RenderHelper.enableStandardItemLighting();
                GL11.glColor4f(1F, 1, 1F, 1);
                GL11.glPopMatrix();
                i++;
            }
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

}
