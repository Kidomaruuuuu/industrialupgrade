package com.denfop.gui;

import com.denfop.api.gui.Component;
import com.denfop.api.gui.EnumTypeComponent;
import com.denfop.api.gui.GuiComponent;
import com.denfop.componets.ComponentRenderInventory;
import com.denfop.componets.EnumTypeComponentSlot;
import com.denfop.componets.EnumTypeStyle;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import net.minecraft.inventory.IInventory;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiIU<T extends ContainerBase<? extends IInventory>> extends GuiIC2<T> {

    private final EnumTypeStyle style;
    List<GuiComponent> componentList = new ArrayList<>();
    protected GuiComponent inventory ;
    protected GuiComponent slots;

    public GuiIU(final T container) {
        super(container);
        this.style = EnumTypeStyle.DEFAULT;
        inventory = new GuiComponent(this,7,83, getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.ALL)));
        this.slots =  new GuiComponent(this,0,0,getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.SLOTS_UPGRADE)));

        componentList.add(inventory);
        componentList.add(slots);
    }
    public GuiIU(final T container,EnumTypeStyle style) {
        super(container);
        this.style = style;
        inventory = new GuiComponent(this,7,83, getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.ALL)));
        this.slots =  new GuiComponent(this,0,0,getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.SLOTS_UPGRADE)));

        componentList.add(inventory);
        componentList.add(slots);
    }
    public GuiIU(final T container,EnumTypeComponent style) {
        super(container);
        this.style = getStyle(style);
        inventory = new GuiComponent(this,7,83, getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.ALL)));
        this.slots =  new GuiComponent(this,0,0,getComponent(),
                new Component<>(new ComponentRenderInventory(EnumTypeComponentSlot.SLOTS_UPGRADE)));

        componentList.add(inventory);
        componentList.add(slots);
    }
    public EnumTypeComponent getComponent(){
        switch (this.style){
            case ADVANCED:
                return EnumTypeComponent.ADVANCED;
            case IMPROVED:
                return EnumTypeComponent.IMPROVED;
            case PERFECT:
                return EnumTypeComponent.PERFECT;
            default:
                return EnumTypeComponent.DEFAULT;
        }
    }
    public EnumTypeStyle getStyle(EnumTypeComponent style){
        switch (style){
            case ADVANCED:
                return EnumTypeStyle.ADVANCED;
            case IMPROVED:
                return EnumTypeStyle.IMPROVED;
            case PERFECT:
                return EnumTypeStyle.PERFECT;
            default:
                return EnumTypeStyle.DEFAULT;
        }
    }
    public EnumTypeStyle getStyle(){
return this.style;
    }
    public void addComponent(GuiComponent component){
        componentList.add(component);
    }
    public void removeComponent(int index){
        componentList.remove(index);
    }
    public void removeComponent(GuiComponent component){
        componentList.remove(component);
    }
    public void drawForeground(int mouseX, int mouseY){
        componentList.forEach(guiComponent -> guiComponent.drawForeground(mouseX,mouseY));
    }
    public void drawBackground(){
        int xoffset = (this.width - this.xSize) / 2;
        int yoffset = (this.height - this.ySize) / 2;
        componentList.forEach(guiComponent -> guiComponent.drawBackground(xoffset,yoffset));

    }
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks,mouseX,mouseY);
        this.drawBackground();
    }
    protected void drawForegroundLayer(int par1, int par2) {
        super.drawForegroundLayer(par1, par2);
        this.drawForeground(par1, par2);
    }
}
