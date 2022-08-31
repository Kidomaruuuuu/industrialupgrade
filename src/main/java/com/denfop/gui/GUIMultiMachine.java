package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.api.gui.Component;
import com.denfop.api.gui.EnumTypeComponent;
import com.denfop.api.gui.GuiComponent;
import com.denfop.api.recipe.InvSlotMultiRecipes;
import com.denfop.componets.ComponentProcessRender;
import com.denfop.container.ContainerMultiMachine;
import ic2.core.gui.GuiElement;
import ic2.core.init.Localization;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiMultiMachine extends GuiIU<ContainerMultiMachine> {

    private final ContainerMultiMachine container;
    private final GuiComponent process;

    public GuiMultiMachine(ContainerMultiMachine container1) {
        super(container1, container1.base.getMachine().getComponent());
        this.container = container1;
        this.process = new GuiComponent(this, 0, 0, EnumTypeComponent.MULTI_PROCESS,
                new Component<>(new ComponentProcessRender(container1.base.multi_process, container1.base.getTypeMachine()))
        );
        this.addComponent(new GuiComponent(this, 8, 48, EnumTypeComponent.ENERGY_CLASSIC,
                new Component<>(this.container.base.energy)
        ));
        this.addComponent(new GuiComponent(this, 17, 48, EnumTypeComponent.ENERGY_RF_CLASSIC,
                new Component<>(this.container.base.energy2)
        ));
        this.addComponent(new GuiComponent(this, 27, 47, EnumTypeComponent.COLD,
                new Component<>(this.container.base.cold)
        ));
        this.addComponent(new GuiComponent(this, 34, 47, EnumTypeComponent.EXP,
                new Component<>(this.container.base.exp)
        ));
    }

    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.drawForeground(mouseX, mouseY);
        int i = 0;
        for (Slot slot : this.container.inventorySlots) {
            if (slot instanceof SlotInvSlot) {
                int xX = slot.xPos;
                int yY = slot.yPos;
                SlotInvSlot slotInv = (SlotInvSlot) slot;
                if (slotInv.invSlot instanceof InvSlotMultiRecipes) {
                    this.process.setIndex(i);
                    this.process.setX(xX);
                    this.process.setY(yY + 19);
                    this.process.drawForeground(mouseX, mouseY);
                    i++;
                }

            }
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
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        this.drawBackground();
        int i = 0;
        for (Slot slot : this.container.inventorySlots) {
            if (slot instanceof SlotInvSlot) {
                int xX = slot.xPos;
                int yY = slot.yPos;
                SlotInvSlot slotInv = (SlotInvSlot)slot;
                if (slotInv.invSlot instanceof com.denfop.api.recipe.InvSlotMultiRecipes) {
                    this.process.setIndex(i);
                    this.process.setX(xX);
                    this.process.setY(yY + 19);
                    this.process.drawBackground(xoffset, yoffset);
                    i++;
                }
            }
        }
        this.mc.getTextureManager().bindTexture(getTexture());
        this.drawXCenteredString(this.xSize / 2, 6, Localization.translate(this.container.base.getName()), 4210752, false);
        for (final GuiElement<?> guiElement : this.elements) {
            if (guiElement.isEnabled()) {
                guiElement.drawBackground(x - this.guiLeft, y - this.guiTop);
            }
        }


    }


    public ResourceLocation getTexture() {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/guimachine_main.png");
    }

}
