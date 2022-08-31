package com.denfop.tiles.mechanism;

import com.denfop.IUItem;
import com.denfop.Ic2Items;
import com.denfop.api.Recipes;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.Input;
import com.denfop.api.recipe.MachineRecipe;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.container.ContainerHandlerHeavyOre;
import com.denfop.gui.GuiHandlerHeavyOre;
import com.denfop.tiles.base.TileEntityBaseHandlerHeavyOre;
import com.denfop.utils.ModUtils;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.type.ResourceBlock;
import ic2.core.init.Localization;
import ic2.core.ref.BlockName;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TileEntityHandlerHeavyOre extends TileEntityBaseHandlerHeavyOre {

    private boolean auto;

    public TileEntityHandlerHeavyOre() {
        super(1, 300, 3);
    }

    public static void init() {
        addhandlerore(
                new ItemStack(IUItem.heavyore),
                new ItemStack[]{new ItemStack(Blocks.IRON_ORE), new ItemStack(Blocks.GOLD_ORE)},
                (short) 1500, 75, 25
        );
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 1), new ItemStack[]{new ItemStack(IUItem.ore, 1, 7),
                        new ItemStack(Blocks.GOLD_ORE), BlockName.resource.getItemStack(ResourceBlock.copper_ore)},
                (short) 3000, 28, 44, 28
        );
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 2), new ItemStack[]{new ItemStack(IUItem.ore, 1, 11),
                BlockName.resource.getItemStack(ResourceBlock.lead_ore)}, (short) 5000, 13, 87);
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 3),
                new ItemStack[]{new ItemStack(IUItem.ore, 1, 8), new ItemStack(IUItem.ore, 1, 6)},
                (short) 4000, 44, 56
        );
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 4),
                new ItemStack[]{new ItemStack(Blocks.IRON_ORE), new ItemStack(IUItem.ore, 1, 4)},
                (short) 2500, 80, 20
        );
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 5),
                new ItemStack[]{new ItemStack(Blocks.QUARTZ_ORE), new ItemStack(
                        IUItem.ore,
                        1, 12
                )},
                (short) 2500, 84, 16
        );
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 6),
                new ItemStack[]{BlockName.resource.getItemStack(ResourceBlock.uranium_ore),
                        new ItemStack(IUItem.toriyore)},
                (short) 4500, 84, 16
        );
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 7),
                new ItemStack[]{BlockName.resource.getItemStack(ResourceBlock.copper_ore), new ItemStack(Blocks.LAPIS_ORE),
                        new ItemStack(Blocks.REDSTONE_ORE)},
                (short) 2000, 55, 23, 21
        );

        addhandlerore(new ItemStack(IUItem.heavyore, 1, 8), new ItemStack[]{new ItemStack(IUItem.ore, 1, 13),
                        new ItemStack(IUItem.ore, 1, 5), new ItemStack(Blocks.IRON_ORE)}, (short) 3000
                , 44, 28, 28);
        addhandlerore(
                new ItemStack(IUItem.heavyore, 1, 9),
                new ItemStack[]{new ItemStack(IUItem.ore, 1, 4), new ItemStack(IUItem.ore, 1, 6)},
                (short) 3500, 50, 50
        );
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 10), new ItemStack[]{new ItemStack(IUItem.ore, 1, 8),
                        new ItemStack(IUItem.toriyore), BlockName.resource.getItemStack(ResourceBlock.uranium_ore)}, (short) 3000, 50, 25
                , 25);
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 11), new ItemStack[]{new ItemStack(IUItem.ore, 1, 12),
                new ItemStack(Blocks.COAL_ORE)}, (short) 4000, 65, 35);

        addhandlerore(new ItemStack(IUItem.heavyore, 1, 12), new ItemStack[]{new ItemStack(IUItem.ore, 1, 8),
                new ItemStack(Blocks.IRON_ORE)}, (short) 4500, 47, 53);
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 13), new ItemStack[]{new ItemStack(IUItem.ore, 1, 13),
                new ItemStack(IUItem.ore, 1, 5), new ItemStack(IUItem.ore, 1, 1)}, (short) 4000, 66, 17, 17);
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 14), new ItemStack[]{new ItemStack(Blocks.IRON_ORE),
                new ItemStack(IUItem.ore, 1, 5)}, (short) 4000, 60, 40);
        addhandlerore(new ItemStack(IUItem.heavyore, 1, 15), new ItemStack[]{new ItemStack(IUItem.ore, 1, 3),
                Ic2Items.tinOre}, (short) 4000, 80, 20);


    }

    public static void addhandlerore(ItemStack container, ItemStack[] output, short temperature, int... col) {
        NBTTagCompound nbt = ModUtils.nbt();
        nbt.setShort("temperature", temperature);
        for (int i = 0; i < col.length; i++) {
            nbt.setInteger("input" + i, col[i]);
        }
        final IRecipeInputFactory input = ic2.api.recipe.Recipes.inputFactory;
        Recipes.recipes.addRecipe(
                "handlerho",
                new BaseMachineRecipe(
                        new Input(
                                input.forStack(container)
                        ),
                        new RecipeOutput(nbt, output)
                )
        );


    }

    protected List<ItemStack> getWrenchDrops(EntityPlayer player, int fortune) {
        List<ItemStack> ret = super.getWrenchDrops(player, fortune);
        if (this.auto) {
            ret.add(new ItemStack(IUItem.autoheater));
            this.auto = false;
        }
        return ret;
    }

    public void updateEntityServer() {
        super.updateEntityServer();
        if (!this.inputSlotA.isEmpty() && output != null && this.outputSlot.canAdd(this.output.getRecipe().output.items) && this.energy.getEnergy() >= this.energyConsume && output.getRecipe().output.metadata != null) {

            if (output.getRecipe().output.metadata.getShort("temperature") == 0 || output.getRecipe().output.metadata.getInteger(
                    "temperature") > this.heat.getEnergy()) {
                if(!( this).heat.need)
                    ( this).heat.need = true;
                return;

            } else
            if(( this).heat.need)
                ( this).heat.need = false;
            ( this).heat.storage--;

            if (!this.getActive()) {
                setActive(true);
            }
            if (this.progress == 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
            }
            this.progress = (short) (this.progress + 1);
            this.energy.useEnergy(energyConsume);
            double k = this.progress;

            this.guiProgress = (k / this.operationLength);
            if (this.progress >= this.operationLength) {
                this.guiProgress = 0;
                operate(output);
                this.progress = 0;
                IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
            }
        } else {
            if (this.progress != 0 && getActive()) {
                IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
            }
            if (output == null) {
                this.progress = 0;
            }
            if (this.getActive()) {
                setActive(false);
            }
            if (this.heat.getEnergy() > 0) {
                this.heat.useEnergy(1);
            }

        }

        if (this.auto) {
            if (this.heat.getEnergy() + 1 <= this.heat.getCapacity()) {
                this.heat.addEnergy(2);
            }
        }
        if (output == null )
            ( this).heat.useEnergy(1);
        if ((!this.inputSlotA.isEmpty() || !this.outputSlot.isEmpty()) && this.upgradeSlot.tickNoMark()) {
            setOverclockRates();
        }

    }

    @Override
    protected boolean onActivated(
            final EntityPlayer player,
            final EnumHand hand,
            final EnumFacing side,
            final float hitX,
            final float hitY,
            final float hitZ
    ) {
        final ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem().equals(IUItem.autoheater) && !this.auto) {
            this.auto = true;
            stack.shrink(1);
        }
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("auto", this.auto);
        return nbttagcompound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.auto = nbttagcompound.getBoolean("auto");
    }


    public String getInventoryName() {

        return Localization.translate("iu.handler.name");
    }

    @Override
    public ContainerBase<? extends TileEntityBaseHandlerHeavyOre> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerHandlerHeavyOre(entityPlayer, this);

    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiHandlerHeavyOre(new ContainerHandlerHeavyOre(entityPlayer, this));
    }

    public String getStartSoundFile() {
        return "Machines/handlerho.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }

    public float getWrenchDropRate() {
        return 0.85F;
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer,
                UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing
        );
    }

    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
        }
        inputSlotA.load();
        this.getOutput();
        if(this.output == null)
            ( this).heat.need = false;
    }

    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }

    }


    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
        }

    }


    @Override
    public void onUpdate() {

    }

    @Override
    public MachineRecipe getRecipeOutput() {
        return this.output;
    }

    @Override
    public void setRecipeOutput(final MachineRecipe output) {
        this.output = output;
    }

}
