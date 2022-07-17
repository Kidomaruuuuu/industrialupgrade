package com.denfop.tiles.mechanism.multimechanism.simple;

import com.denfop.api.recipe.MachineRecipe;
import com.denfop.tiles.base.EnumMultiMachine;
import com.denfop.tiles.base.TileEntityMultiMachine;
import ic2.core.init.Localization;


public class TileEntityRecycler extends TileEntityMultiMachine {

    public TileEntityRecycler() {
        super(
                EnumMultiMachine.RECYCLER.usagePerTick,
                EnumMultiMachine.RECYCLER.lenghtOperation,
                1,
                8,
                true,
                4
        );
    }

    public void initiate(int soundEvent) {
        if (this.getWorld().provider.getWorldTime() % 40 == 0) {
            super.initiate(soundEvent);
        }
    }

    public void operate(int slotId, MachineRecipe output, int size) {
        for (int i = 0; i < this.operationsPerTick; i++) {
            operateOnce(slotId, output.getRecipe().output.items, size);
            if (this.inputSlots.get(slotId).isEmpty() || !this.outputSlot.canAdd(output.getRecipe().output.items)) {
                this.getOutput(slotId);
                break;
            }

        }
    }

    public MachineRecipe getOutput(int slotId) {

        if (this.inputSlots.isEmpty(slotId)) {
            this.output[slotId] = null;
            return null;
        }
        this.output[slotId] = this.inputSlots.process(slotId);
        if (output[slotId] == null) {
            output[slotId] = this.inputSlots.recycler_output;

        }
        if (this.outputSlot.canAdd(output[slotId].getRecipe().output.items)) {
            return output[slotId];
        }

        return null;
    }

    @Override
    public EnumMultiMachine getMachine() {
        return EnumMultiMachine.RECYCLER;
    }

    public String getInventoryName() {
        return Localization.translate("iu.blockRecycler.name");
    }

    public String getStartSoundFile() {
        return "Machines/RecyclerOp.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }


}
