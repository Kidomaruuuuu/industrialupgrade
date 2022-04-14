package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.api.inv.IInvSlotProcessableMulti;
import com.denfop.container.ContainerMultiMachine;
import com.denfop.container.ContainerMultiMetalFormer;
import com.denfop.tiles.base.TileEntityMultiMachine;
import com.denfop.utils.ModUtils;
import ic2.core.GuiIC2;
import ic2.core.block.wiring.CableType;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIMultiMachine4 extends GuiIC2<ContainerMultiMachine> {

    private final ContainerMultiMachine container;

    public GUIMultiMachine4(ContainerMultiMachine container1) {
        super(container1);
        this.container = container1;
        if (container1 instanceof ContainerMultiMetalFormer) {
            this.addElement((new VanillaButton(this, 18, 8, 20, 20, this.createEventSender(0))).withIcon(() -> {
                switch (container1.base.getMode()) {
                    case 1:
                        return ItemName.forge_hammer.getItemStack();
                    case 2:
                        return ItemName.cutter.getItemStack();
                    default:
                        return ItemName.cable.getItemStack(CableType.copper);
                }
            }).withTooltip(() -> {
                switch (container1.base.getMode()) {
                    case 0:
                        return Localization.translate("ic2.MetalFormer.gui.switch.Extruding");
                    case 1:
                        return Localization.translate("ic2.MetalFormer.gui.switch.Rolling");
                    case 2:
                        return Localization.translate("ic2.MetalFormer.gui.switch.Cutting");
                    default:
                        return null;
                }
            }));
        }
    }

    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(getTexture());
        int j = (this.width - this.xSize) / 2;
        int k = (this.height - this.ySize) / 2;

        drawTexturedModalRect(j, k, 0, 0, this.xSize, this.ySize);
        this.mc.getTextureManager().bindTexture(new ResourceLocation("ic2", "textures/gui/infobutton.png"));
        this.drawTexturedRect(3.0D, 3.0D, 10.0D, 10.0D, 0.0D, 0.0D);
        this.mc.getTextureManager().bindTexture(getTexture());
        TileEntityMultiMachine tile = this.container.base;
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        int chargeLevel = (int) (14.0F * tile.getChargeLevel2());
        int chargeLevel1 = (int) (14.0F * tile.getChargeLevel1());

        int i = 0;
        for (Slot slot : this.container.inventorySlots) {
            if (slot instanceof SlotInvSlot) {
                int xX = xoffset + slot.xPos;
                int yY = yoffset + slot.yPos;
                SlotInvSlot slotInv = (SlotInvSlot) slot;
                if (slotInv.invSlot instanceof IInvSlotProcessableMulti) {
                    int down = 24;
                    if (tile.getMachine().meta < 5) {
                        down = 24 * (tile.getMachine().meta);
                    }
                    drawTexturedModalRect(xX, yY + 19, 176, 14 + down, 16, 24);
                    int progress = (int) (24.0F * tile.getProgress(i));
                    if (progress >= 0) {
                        drawTexturedModalRect(xX, yY + 19, 192, 14 + down, 16, progress + 1);
                    }
                    i++;

                }
                drawTexturedModalRect(xX - 1, yY - 1, 238, 0, 18, 18);
            }
        }
        int exp = (int) (24.0F * tile.expstorage / 5000);
        if (exp > 0) {

            drawTexturedModalRect(xoffset + 9, yoffset + 26, 176, 134, exp + 1, 16);

        }

        if (chargeLevel >= 0) {
            drawTexturedModalRect(
                    xoffset + 5, yoffset + 47 + 14 - chargeLevel, 176, 14 - chargeLevel, 14,
                    chargeLevel
            );
        }
        if (chargeLevel1 >= 0) {
            drawTexturedModalRect(
                    xoffset + 5 + 9, yoffset + 47 + 14 - chargeLevel1, 176 + 14, 14 - chargeLevel1, 14,
                    chargeLevel1
            );
        }
        this.drawXCenteredString(this.xSize / 2, 6, Localization.translate(this.container.base.getName()), 4210752, false);
        String tooltip1 = ModUtils.getString(this.container.base.energy2) + "/" + ModUtils.getString(this.container.base.maxEnergy2) + " RF";
        String tooltip2 =
                ModUtils.getString(this.container.base.energy.getEnergy()) + "/" + ModUtils.getString(this.container.base.energy.getCapacity()) + " " +
                        "EU";

        GuiTooltipHelper.drawAreaTooltip(this, x - this.guiLeft, y - this.guiTop, tooltip2, 5, 47, 19, 61);
        GuiTooltipHelper.drawAreaTooltip(this, x - this.guiLeft, y - this.guiTop, tooltip1, 14, 47, 28, 61);
        String tooltip = this.container.base.expstorage + "/" + 5000;

        GuiTooltipHelper.drawAreaTooltip(this, x - this.guiLeft, y - this.guiTop, tooltip, 9, 30, 32, 38);
        i = 0;
        for (Slot slot : this.container.inventorySlots) {
            if (slot instanceof SlotInvSlot) {
                int xX = slot.xPos;
                int yY = slot.yPos;
                SlotInvSlot slotInv = (SlotInvSlot) slot;
                if (slotInv.invSlot instanceof IInvSlotProcessableMulti) {

                    double progress = (24.0F * this.container.base.getProgress(i));
                    if (progress > 0) {
                        GuiTooltipHelper.drawAreaTooltip(this, x - this.guiLeft, y - this.guiTop,
                                ModUtils.getString(this.container.base.getProgress(i) * 100) + "%", xX, yY + 19, xX + 16,
                                yY + 19 + 25
                        );
                    }
                    i++;
                }

            }
        }


    }


    public ResourceLocation getTexture() {
        TileEntityMultiMachine tile = this.container.base;
        if (this.container.base.getMachine().meta < 5) {
            String type = "";
            if (tile.progress.length == 2) {
                type = "_adv";
            }
            if (tile.progress.length == 3) {
                type = "_imp";
            }
            if (tile.progress.length == 4) {
                type = "_per";
            }
            return new ResourceLocation(Constants.MOD_ID, "textures/gui/GUIMachine" + type + ".png");
        } else {
            String type = "";
            if (tile.progress.length == 2) {
                type = "_adv";
            }
            if (tile.progress.length == 3) {
                type = "_imp";
            }
            if (tile.progress.length == 4) {
                type = "_per";
            }
            return new ResourceLocation(Constants.MOD_ID, "textures/gui/GUIMachine2" + type + ".png");
        }
    }

}
