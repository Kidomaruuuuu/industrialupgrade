package com.denfop.api.gui;

import com.denfop.componets.AdvEnergy;
import com.denfop.componets.ComponentProcessRender;
import com.denfop.componets.CoolComponent;
import com.denfop.componets.EXPComponent;
import com.denfop.componets.RFComponent;
import com.denfop.utils.ModUtils;

public class Component<T> {

    private final T component;

    public Component(T component) {
        this.component = component;
    }

    public T getComponent() {
        return component;
    }

    public void drawBackground(int mouseX, int mouseY, final GuiComponent guiComponent) {
        if (this.component instanceof AdvEnergy) {
            AdvEnergy component = (AdvEnergy) this.component;
            double fillratio = component.storage / component.capacity;
            if (guiComponent.getType().getRender() == EnumTypeRender.HEIGHT) {
                fillratio *= guiComponent.getType().getHeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY() + guiComponent
                                .getType()
                                .getHeight() - chargeLevel,
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1() + guiComponent.getType().getHeight() - chargeLevel,
                        guiComponent.getType().getWeight(),
                        chargeLevel
                );
            } else {
                fillratio *= guiComponent.getType().getWeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY(),
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1(),
                        chargeLevel + 1,
                        guiComponent.getType().getHeight()
                );
            }

        } else if (this.component instanceof RFComponent) {
            RFComponent component = (RFComponent) this.component;
            double fillratio = component.storage / component.capacity;
            if (guiComponent.getType().getRender() == EnumTypeRender.HEIGHT) {
                fillratio *= guiComponent.getType().getHeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY() + guiComponent
                                .getType()
                                .getHeight() - chargeLevel,
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1() + guiComponent.getType().getHeight() - chargeLevel,
                        guiComponent.getType().getWeight(),
                        chargeLevel
                );
            } else {
                fillratio *= guiComponent.getType().getWeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY(),
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1(),
                        chargeLevel + 1,
                        guiComponent.getType().getHeight()
                );
            }
        } else if (this.component instanceof CoolComponent) {
            CoolComponent component = (CoolComponent) this.component;
            double fillratio = component.storage / component.capacity;
            if (guiComponent.getType().getRender() == EnumTypeRender.HEIGHT) {
                fillratio *= guiComponent.getType().getHeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY() + guiComponent
                                .getType()
                                .getHeight() - chargeLevel,
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1() + guiComponent.getType().getHeight() - chargeLevel,
                        guiComponent.getType().getWeight(),
                        chargeLevel
                );
            } else {
                fillratio *= guiComponent.getType().getWeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY(),
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1(),
                        chargeLevel + 1,
                        guiComponent.getType().getHeight()
                );
            }
        } else if (this.component instanceof EXPComponent) {
            EXPComponent component = (EXPComponent) this.component;
            double fillratio = component.storage / component.capacity;
            if (guiComponent.getType().getRender() == EnumTypeRender.HEIGHT) {
                fillratio *= guiComponent.getType().getHeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY() + guiComponent
                                .getType()
                                .getHeight() - chargeLevel,
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1() + guiComponent.getType().getHeight() - chargeLevel,
                        guiComponent.getType().getWeight(),
                        chargeLevel
                );
            } else {
                fillratio *= guiComponent.getType().getWeight();
                int chargeLevel = (int) fillratio;
                guiComponent.getGui().drawTexturedModalRect(
                        mouseX + guiComponent.getX() + guiComponent.getType().getEndX(),
                        mouseY + guiComponent.getType().getEndY() + guiComponent.getY(),
                        guiComponent.getType().getX1(),
                        guiComponent.getType().getY1(),
                        chargeLevel + 1,
                        guiComponent.getType().getHeight()
                );
            }
        }
    }

    public String getText(final GuiComponent guiComponent) {
        String text = "";

        if (this.component instanceof AdvEnergy) {

            AdvEnergy component = (AdvEnergy) this.component;
            text =
                    ModUtils.getString(Math.min(
                            component.getEnergy(),
                            component.getCapacity()
                    )) + "/" + ModUtils.getString(component.getCapacity()) + " " +
                            "EU";

        } else if (this.component instanceof RFComponent) {
            RFComponent component = (RFComponent) this.component;
            text =
                    ModUtils.getString(Math.min(
                            component.getEnergy(),
                            component.getCapacity()
                    )) + "/" + ModUtils.getString(component.getCapacity()) + " " +
                            "RF";

        } else if (this.component instanceof ComponentProcessRender) {
            ComponentProcessRender component = (ComponentProcessRender) this.component;
            text = ModUtils.getString(component.getProcess().getProgress(guiComponent.getIndex()) * 100) + "%";
        } else if (this.component instanceof CoolComponent) {
            CoolComponent component = (CoolComponent) this.component;
            text =
                    ModUtils.getString(component
                            .getEnergy()) + "°C" + "/" + ModUtils.getString(component.getCapacity()) + "°C";


        } else if (this.component instanceof EXPComponent) {
            EXPComponent component = (EXPComponent) this.component;
            text = "EXP: " +
                    ModUtils.getString(component
                            .getEnergy()) + "/" + ModUtils.getString(component.getCapacity());

        }


        return text;
    }

}
