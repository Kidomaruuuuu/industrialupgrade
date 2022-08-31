package com.denfop.items;

import com.denfop.Constants;
import com.denfop.IUCore;
import com.denfop.IUItem;
import com.denfop.api.Recipes;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.Input;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.api.windsystem.IWindRotor;
import com.denfop.api.windsystem.upgrade.IRotorUpgradeItem;
import com.denfop.api.windsystem.upgrade.RotorUpgradeItemInform;
import com.denfop.api.windsystem.upgrade.RotorUpgradeSystem;
import com.denfop.api.windsystem.upgrade.event.EventRotorItemLoad;
import com.denfop.items.reactors.ItemGradualInt;
import com.denfop.utils.ModUtils;
import ic2.api.energy.EnergyNet;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.core.init.Localization;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;


public class ItemAdvancedWindRotor extends ItemGradualInt implements IWindRotor, IRotorUpgradeItem {

    private final int radius;
    private final float efficiency;
    private final ResourceLocation renderTexture;
    private final int level;
    private final int index;
    private final int tier;

    public ItemAdvancedWindRotor(
            String name, int Radius, int durability, float efficiency,
            ResourceLocation RenderTexture, int level, int index
    ) {
        super(name, durability);
        this.setCreativeTab(IUCore.ItemTab);
        this.setMaxStackSize(1);
        this.radius = Radius;
        this.efficiency = efficiency;
        this.renderTexture = RenderTexture;
        this.level = level;
        this.index = index;
        double KU1 = 20 * efficiency * 27.0F;
        this.tier = EnergyNet.instance.getTierFromPower(KU1);
        final IRecipeInputFactory input = ic2.api.recipe.Recipes.inputFactory;
        for(int i = 0; i<11;i++)
        Recipes.recipes.addRecipe(
                "rotor_upgrade",
                new BaseMachineRecipe(
                        new Input(
                                input.forStack(new ItemStack(this,1,0)),
                                input.forStack(new ItemStack(IUItem.rotors_upgrade,1, i))
                        ),
                        new RecipeOutput(null, new ItemStack(this,1,0))
                )
        );
    }

    @SideOnly(Side.CLIENT)
    public static void registerModel(Item item, int meta, String name) {
        ModelLoader.setCustomModelResourceLocation(item, meta, getModelLocation(name));
    }

    @SideOnly(Side.CLIENT)
    public static ModelResourceLocation getModelLocation(String name) {

        final String loc = Constants.MOD_ID +
                ':' +
                "rotor" + "/" + name;
        return new ModelResourceLocation(loc, null);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack itemStack, @Nonnull World world, @Nonnull Entity entity, int slot, boolean par5) {
        NBTTagCompound nbt = ModUtils.nbt(itemStack);

        if (!RotorUpgradeSystem.instance.hasInMap(itemStack)) {
            nbt.setBoolean("hasID", false);
            MinecraftForge.EVENT_BUS.post(new EventRotorItemLoad(world, this, itemStack));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(String name) {
        this.registerModel(0, name, null);
    }

    @SideOnly(Side.CLIENT)
    protected void registerModel(int meta, String name) {
        registerModel(this, meta, name);
    }

    @SideOnly(Side.CLIENT)
    protected void registerModel(int meta, String name, String extraName) {
        registerModel(this, meta, name);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, List<String> tooltip, @Nonnull ITooltipFlag advanced) {
        int windStrength = 10;
        int windStrength1 = 20;
        double KU = windStrength * this.getEfficiency(stack) * 27.0F;


        double KU1 = windStrength1 * this.getEfficiency(stack) * 27.0F;

        tooltip.add(Localization.translate("iu.windgenerator") + windStrength + " m/s " + Localization.translate("iu" +
                ".windgenerator1") + ModUtils.getString(KU));
        tooltip.add(Localization.translate("iu.windgenerator") + windStrength1 + " m/s " + Localization.translate("iu" +
                ".windgenerator1") + ModUtils.getString(KU1));
        tooltip.add(Localization.translate("gui.iu.tier")+": " + this.getLevel());

        if (RotorUpgradeSystem.instance.hasInMap(stack)) {
            final List<RotorUpgradeItemInform> lst = RotorUpgradeSystem.instance.getInformation(stack);
            final int col = RotorUpgradeSystem.instance.getRemaining(stack);
            if (!lst.isEmpty()) {
                for (RotorUpgradeItemInform upgrade : lst) {
                    tooltip.add(upgrade.getName());
                }
            }
            if (col != 0) {
                tooltip.add(Localization.translate("free_slot") + col + Localization.translate(
                        "free_slot1"));
            } else {
                tooltip.add(Localization.translate("not_free_slot"));

            }


        }

    }

    public int getDiameter(ItemStack stack) {
        return this.radius;
    }

    public ResourceLocation getRotorRenderTexture(ItemStack stack) {
        return this.renderTexture;
    }

    public float getEfficiency(ItemStack stack) {
        return this.efficiency;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getSourceTier() {
        return tier;
    }


}
