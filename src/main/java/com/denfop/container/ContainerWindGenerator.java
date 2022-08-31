package com.denfop.container;

import com.denfop.tiles.mechanism.wind.TileEntityWindGenerator;
import ic2.core.ContainerFullInv;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class ContainerWindGenerator extends ContainerFullInv<TileEntityWindGenerator> {

    public ContainerWindGenerator(TileEntityWindGenerator windGenerator, EntityPlayer entityPlayer) {
        super(entityPlayer, windGenerator, 236);
        this.addSlotToContainer(new SlotInvSlot(windGenerator.slot, 0, 89, 19));

    }

    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("coefficient");
        ret.add("speed");
        ret.add("slot");
        ret.add("rotorSide");
        ret.add("generation");
        ret.add("energy");
        return ret;
    }

}
