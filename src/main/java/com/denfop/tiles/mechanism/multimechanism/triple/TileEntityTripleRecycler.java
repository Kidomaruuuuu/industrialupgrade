package com.denfop.tiles.mechanism.multimechanism.triple;

import com.denfop.Ic2Items;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.Input;
import com.denfop.api.recipe.MachineRecipe;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.tiles.base.EnumMultiMachine;
import com.denfop.tiles.base.TileEntityMultiMachine;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.core.init.Localization;

import java.util.Collections;

public class TileEntityTripleRecycler extends TileEntityMultiMachine {

    public TileEntityTripleRecycler() {
        super(
                EnumMultiMachine.TRIPLE_RECYCLER.usagePerTick,
                EnumMultiMachine.TRIPLE_RECYCLER.lenghtOperation,
                1,
                2,
                true,
                1
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
            final IRecipeInputFactory input = ic2.api.recipe.Recipes.inputFactory;
            output[slotId] = new MachineRecipe(new BaseMachineRecipe(
                    new Input(input.forStack(this.inputSlots.get(slotId))),
                    new RecipeOutput(
                            null,
                            Ic2Items.scrap
                    )
            ), Collections.singletonList(1));

        }
        if (this.outputSlot.canAdd(output[slotId].getRecipe().output.items)) {
            return output[slotId];
        }

        return null;
    }

    @Override
    public EnumMultiMachine getMachine() {
        return EnumMultiMachine.TRIPLE_RECYCLER;
    }

    public String getInventoryName() {
        return Localization.translate("iu.blockRecycler1.name");
    }

    public String getStartSoundFile() {
        return "Machines/RecyclerOp.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }


}
