package com.denfop.tiles.base;

import com.denfop.IUItem;
import com.denfop.api.Recipes;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.Input;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.api.upgrade.IUpgradeItem;
import com.denfop.api.upgrade.IUpgradeWithBlackList;
import com.denfop.api.upgrade.UpgradeItemInform;
import com.denfop.api.upgrade.UpgradeSystem;
import com.denfop.api.upgrade.event.EventItemBlackListLoad;
import com.denfop.api.upgrade.event.EventItemLoad;
import com.denfop.container.ContainerDoubleElectricMachine;
import com.denfop.gui.GuiUpgradeBlock;
import com.denfop.items.EnumInfoUpgradeModules;
import com.denfop.items.modules.ItemQuarryModule;
import com.denfop.items.modules.ItemUpgradeModule;
import com.denfop.utils.ModUtils;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.init.Localization;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denfop.events.IUEventHandler.getUpgradeItem;

public class TileEntityUpgradeBlock extends TileEntityDoubleElectricMachine {

    public TileEntityUpgradeBlock() {
        super(1, 300, 1, Localization.translate("blockUpgrade.name"), EnumDoubleElectricMachine.UPGRADE);
    }

    public static void init() {

    }

    public static void addupgrade(Item container, ItemStack fill) {
        NBTTagCompound nbt = ModUtils.nbt();
        nbt.setString(
                "mode_module",
                fill.getItem() instanceof ItemUpgradeModule ? ItemUpgradeModule.getType(fill.getItemDamage()).name : "blacklist"
        );
        final IRecipeInputFactory input = ic2.api.recipe.Recipes.inputFactory;
        Recipes.recipes.addRecipe(
                "upgradeblock",
                new BaseMachineRecipe(
                        new Input(
                                input.forStack(new ItemStack(container, 1, OreDictionary.WILDCARD_VALUE)),
                                input.forStack(fill)
                        ),
                        new RecipeOutput(nbt, new ItemStack(container, 1, OreDictionary.WILDCARD_VALUE))
                )
        );
    }


    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return new ItemStack(IUItem.upgradeblock);
    }


    @SideOnly(Side.CLIENT)
    protected boolean shouldSideBeRendered(EnumFacing side, BlockPos otherPos) {
        return false;
    }

    protected boolean isNormalCube() {
        return false;
    }

    protected boolean doesSideBlockRendering(EnumFacing side) {
        return false;
    }

    protected boolean isSideSolid(EnumFacing side) {
        return false;
    }

    protected boolean clientNeedsExtraModelInfo() {
        return true;
    }

    public boolean shouldRenderInPass(int pass) {
        return true;
    }

    @Override
    public void onNetworkUpdate(String field) {

    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiUpgradeBlock(new ContainerDoubleElectricMachine(entityPlayer, this, type));
    }

    @Override
    public BaseMachineRecipe getOutput() {
        if (this.inputSlotA.isEmpty()) {
            return null;
        }

        BaseMachineRecipe output = this.inputSlotA.process();

        ItemStack stack1 = getUpgradeItem(this.inputSlotA.get(0)) ? this.inputSlotA.get(0) : this.inputSlotA.get(1);
        ItemStack module = getUpgradeItem(this.inputSlotA.get(0)) ? this.inputSlotA.get(1) : this.inputSlotA.get(0);


        NBTTagCompound nbt1 = ModUtils.nbt(stack1);


        if (module.getItem() instanceof ItemUpgradeModule) {
            if (!nbt1.getBoolean("canupgrade")) {
                this.energy.addEnergy(energyConsume * operationLength);
                return null;
            }
            EnumInfoUpgradeModules type = ItemUpgradeModule.getType(module.getItemDamage());
            int min = 0;
            for (int i = 0; i < 4; i++) {
                if (nbt1.getString("mode_module" + i).equals(type.name)) {
                    min++;
                }
            }
            if (min >= type.max) {
                this.energy.addEnergy(energyConsume * operationLength);
                return null;
            }
            List<UpgradeItemInform> upgrade = UpgradeSystem.system.getInformation(stack1);
            List<Integer> lst = type.list;
            for (UpgradeItemInform upgrade1 : upgrade) {
                if (lst.contains(upgrade1.upgrade.ordinal())) {
                    return null;
                }
            }
        }
        if (output == null) {
            return null;
        }
        if (this.outputSlot.canAdd(output.output.items)) {
            return output;
        }
        return null;
    }

    public void operateOnce(BaseMachineRecipe output, List<ItemStack> processResult) {

        ItemStack stack1 = getUpgradeItem(this.inputSlotA.get(0)) ? this.inputSlotA.get(0) : this.inputSlotA.get(1);
        ItemStack module = getUpgradeItem(this.inputSlotA.get(0)) ? this.inputSlotA.get(1) : this.inputSlotA.get(0);


        NBTTagCompound nbt1 = ModUtils.nbt(stack1);
        if (module.getItem() instanceof ItemUpgradeModule) {
            int Damage = stack1.getItemDamage();
            double newCharge = ElectricItem.manager.getCharge(stack1);
            final Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.getEnchantments(stack1);
            this.inputSlotA.consume();
            this.outputSlot.add(processResult);
            ItemStack stack = this.outputSlot.get();
            stack.setTagCompound(nbt1);
            NBTTagCompound nbt = ModUtils.nbt(stack);
            String mode = output.output.metadata.getString("mode_module");

            int k = 0;
            for (int i = 0; i < 4; i++) {
                if (nbt.getString("mode_module" + i).isEmpty()) {
                    k = i;
                    break;
                }
            }
            nbt.setString("mode_module" + k, mode);
            ElectricItem.manager.charge(stack, newCharge, Integer.MAX_VALUE, true, false);
            EnchantmentHelper.setEnchantments(enchantmentMap, stack);
            MinecraftForge.EVENT_BUS.post(new EventItemLoad(world, (IUpgradeItem) stack.getItem(), stack));
            stack.setItemDamage(Damage);
        }

        if (module.getItem() instanceof ItemQuarryModule && module.getItemDamage() == 12) {
            int Damage = stack1.getItemDamage();
            NBTTagCompound nbt2 = ModUtils.nbt(module);
            double newCharge = ElectricItem.manager.getCharge(stack1);
            final Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.getEnchantments(stack1);

            this.inputSlotA.consume();
            this.outputSlot.add(processResult);
            ItemStack stack = this.outputSlot.get();
            stack.setTagCompound(nbt1);
            NBTTagCompound nbt = ModUtils.nbt(stack);
            for (int j = 0; j < 18; j++) {
                String l = "number_" + j;
                String temp = nbt2.getString(l);
                nbt.setString(l, temp);
            }
            nbt.setBoolean("list", true);
            ElectricItem.manager.charge(stack, newCharge, Integer.MAX_VALUE, true, false);
            EnchantmentHelper.setEnchantments(enchantmentMap, stack);
            stack.setItemDamage(Damage);
            MinecraftForge.EVENT_BUS.post(new EventItemBlackListLoad(
                    world,
                    (IUpgradeWithBlackList) stack.getItem(),
                    stack,
                    nbt2
            ));

        }
    }


    public String getStartSoundFile() {
        return "Machines/upgrade_block.ogg";
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

}
