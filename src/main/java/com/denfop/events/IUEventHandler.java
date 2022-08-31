package com.denfop.events;


import com.denfop.IUItem;
import com.denfop.api.IItemSoon;
import com.denfop.api.recipe.BaseMachineRecipe;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.api.upgrade.IUpgradeItem;
import com.denfop.api.upgrade.UpgradeItemInform;
import com.denfop.api.upgrade.UpgradeSystem;
import com.denfop.blocks.BlockIUFluid;
import com.denfop.container.ContainerBags;
import com.denfop.items.EnumInfoUpgradeModules;
import com.denfop.items.armour.ItemAdvJetpack;
import com.denfop.items.bags.ItemEnergyBags;
import com.denfop.items.modules.EnumBaseType;
import com.denfop.items.modules.EnumModule;
import com.denfop.items.modules.ItemBaseModules;
import com.denfop.items.modules.ItemEntityModule;
import com.denfop.utils.CapturedMobUtils;
import com.denfop.utils.ListInformationUtils;
import com.denfop.utils.ModUtils;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IUEventHandler {

    final TextFormatting[] name = {TextFormatting.DARK_PURPLE, TextFormatting.YELLOW, TextFormatting.BLUE,
            TextFormatting.RED, TextFormatting.GRAY, TextFormatting.GREEN, TextFormatting.DARK_AQUA, TextFormatting.AQUA};
    final String[] mattertype = {"matter.name", "sun_matter.name", "aqua_matter.name", "nether_matter.name", "night_matter.name", "earth_matter.name", "end_matter.name", "aer_matter.name"};

    public IUEventHandler() {
    }

    public static boolean getUpgradeItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof IUpgradeItem;

    }

    @SubscribeEvent
    public void bag_pickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!(player.openContainer instanceof ContainerBags)) {
            InventoryPlayer inventory = player.inventory;

            for (int i = 0; i < inventory.mainInventory.size(); ++i) {
                ItemStack stack = inventory.mainInventory.get(i);
                if (stack.getItem() instanceof ItemEnergyBags) {
                    if (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.BAGS, stack)) {
                        ItemEnergyBags bags = (ItemEnergyBags) stack.getItem();
                        if (!(event.getItem().getItem().getItem() instanceof ItemEnergyBags)) {
                            if (bags.canInsert(player, stack, event.getItem().getItem())) {
                                bags.insert(player, stack, event.getItem().getItem());
                                return;
                            }
                        }
                    }
                }
            }

        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onViewRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!(event.getState().getBlock() instanceof BlockIUFluid)) {
            return;
        }
        event.setCanceled(true);
        Fluid fluid = ((BlockIUFluid) event.getState().getBlock()).getFluid();
        GL11.glFogi(2917, 2048);
        event.setDensity((float) Util.map(Math.abs(fluid.getDensity()), 20000.0D, 2.0D));
    }

    public void setFly(EntityPlayer player, boolean fly, ItemStack stack) {
        player.capabilities.isFlying = fly;
        player.capabilities.allowFlying = fly;
        player.getEntityData().setBoolean("isFlyActive", fly);
        if (player.getEntityWorld().isRemote && !fly) {
            player.capabilities.setFlySpeed((float) 0.05);
            player.fallDistance = 0;
        } else {
            boolean edit = player.getEntityData().getBoolean("edit_fly");
            if (!edit) {
                int flyspeed = (UpgradeSystem.system.hasModules(
                        EnumInfoUpgradeModules.FLYSPEED,
                        stack
                ) ?
                        UpgradeSystem.system.getModules(
                                EnumInfoUpgradeModules.FLYSPEED,
                                stack
                        ).number : 0);

                if (player.getEntityWorld().isRemote) {
                    player.capabilities.setFlySpeed((float) ((float) 0.1 + 0.05 * flyspeed));
                }
            } else {
                if (player.getEntityWorld().isRemote) {
                    player.capabilities.setFlySpeed(player.getEntityData().getFloat("fly_speed"));
                }
            }
        }
    }

    public boolean canFly(ItemStack stack) {
        return stack
                .getItem() == IUItem.spectral_chestplate || stack
                .getItem() == IUItem.adv_nano_chestplate || (stack
                .getItem() instanceof ItemAdvJetpack && UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.FLY, stack));
    }

    @SubscribeEvent
    public void FlyUpdate(LivingEvent.LivingUpdateEvent event) {

        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        NBTTagCompound nbtData = player.getEntityData();
        if (!player.capabilities.isCreativeMode) {

            if (!player.inventory.armorInventory.get(2).isEmpty()) {
                if (canFly(player.inventory.armorInventory.get(2))) {
                    NBTTagCompound nbtData1 = ModUtils.nbt(player.inventory.armorInventory.get(2));
                    boolean jetpack = nbtData1.getBoolean("jetpack");
                    if (!jetpack) {
                        if (nbtData.getBoolean("isFlyActive")) {
                            nbtData.setBoolean("hasFly", true);
                            setFly(player, false, player.inventory.armorInventory
                                    .get(2));
                        }
                    } else {
                        if (!player.onGround) {
                            if (nbtData1.getBoolean("canFly")) {
                                setFly(player, true, player.inventory.armorInventory
                                        .get(2));
                                nbtData1.setBoolean("canFly", false);
                                nbtData.setBoolean("canjump", false);
                            }
                        } else {
                            if (nbtData.getBoolean("isFlyActive")) {
                                setFly(player, false, player.inventory.armorInventory
                                        .get(2));
                            }
                        }
                    }
                } else if (!player.inventory.armorInventory.get(2).isEmpty()) {
                    if (nbtData.getBoolean("isFlyActive")) {
                        setFly(player, false, player.inventory.armorInventory
                                .get(2));
                    }
                }
            } else {
                if (nbtData.getBoolean("isFlyActive")) {
                    setFly(player, false, player.inventory.armorInventory
                            .get(2));
                }
            }
        } else {
            if (nbtData.getBoolean("isFlyActive")) {
                setFly(player, false, player.inventory.armorInventory
                        .get(2));
            }
        }


    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void addInformItem(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        if (item instanceof IItemSoon) {
            event.getToolTip().add(((IItemSoon) item).getDescription());
        }
        if (item.equals(IUItem.leadbox)) {
            event.getToolTip().add(Localization.translate("iu.radiationbox"));
        }


        if (item.equals(IUItem.tank)) {
            switch (stack.getItemDamage()) {
                case 1:
                    event.getToolTip().add(Localization.translate("iu.storage_fluid") + 160 + " B");
                    break;
                case 2:
                    event.getToolTip().add(Localization.translate("iu.storage_fluid") + 480 + " B");
                    break;
                case 3:
                    event.getToolTip().add(Localization.translate("iu.storage_fluid") + 2560 + " B");
                    break;
                case 0:
                    event.getToolTip().add(Localization.translate("iu.storage_fluid") + 40 + " B");
                    break;

            }
        }
        if (item.equals(IUItem.upgrade_speed_creation) || item.equals(IUItem.autoheater) || item.equals(IUItem.coolupgrade) || item.equals(
                IUItem.module_quickly) || item.equals(
                IUItem.module_stack) || item.equals(IUItem.module_storage) || (item.equals(
                IUItem.module7) && (stack.getItemDamage() == 4 || stack.getItemDamage() == 10))) {
            event.getToolTip().add(Localization.translate("module.wireless"));
        }
        if (item.equals(IUItem.entitymodules) || stack.getItemDamage() == 4) {
            CapturedMobUtils capturedMobUtils = CapturedMobUtils.create(stack);
            if (capturedMobUtils != null) {
                Entity entity = Objects.requireNonNull(capturedMobUtils).getEntity(event.getEntity().getEntityWorld(), true);
                event.getToolTip().add(Objects.requireNonNull(entity).getName());
            }
        }
        if (item.equals(
                IUItem.module_quickly) || item.equals(
                IUItem.module_stack) || item.equals(IUItem.module_storage)) {
            event.getToolTip().add(Localization.translate("using_kit"));
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add(ListInformationUtils.mechanism_info.get(ListInformationUtils.index));
            } else {
                for (String name : ListInformationUtils.mechanism_info) {
                    event.getToolTip().add(name);
                }
            }

        }
        if(item.equals(
                IUItem.coolupgrade)) {
            event.getToolTip().add(Localization.translate("using_kit"));
            final List<String> list = ListInformationUtils.integerListMap.get(stack.getItemDamage());
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add(list.get(ListInformationUtils.index % list.size()));
            } else {
                for (String name : list) {
                    event.getToolTip().add(name);
                }
            }

        }
        if (item.equals(IUItem.autoheater)) {
            event.getToolTip().add(Localization.translate("using_kit"));

            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add(ListInformationUtils.mechanism_info2.get(ListInformationUtils.index2));
            } else {
                for (String name : ListInformationUtils.mechanism_info2) {
                    event.getToolTip().add(name);
                }
            }
        }
        if (item.equals(
                IUItem.upgrade_speed_creation)) {
            event.getToolTip().add(Localization.translate("using_kit"));
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add(ListInformationUtils.mechanism_info1.get(ListInformationUtils.index1));
            } else {
                for (String name : ListInformationUtils.mechanism_info1) {
                    event.getToolTip().add(name);
                }
            }
        }
        if (stack.getItem().equals(IUItem.analyzermodule)) {
            event.getToolTip().add(Localization.translate("iu.analyzermodule"));
        }
        if (stack.getItem().equals(IUItem.quarrymodule)) {
            event.getToolTip().add(Localization.translate("iu.quarrymodule"));
        }
        if (stack.getItem().equals(IUItem.expmodule)) {
            event.getToolTip().add(Localization.translate("iu.expierence_module.info"));
        }

        if (stack.getItem() instanceof ItemEntityModule) {
            int meta = stack.getItemDamage();
            if (meta == 0) {
                event.getToolTip().add(Localization.translate("iu.entitymodule"));
            }
            if (meta == 1) {
                event.getToolTip().add(Localization.translate("iu.entitymodule1"));
            }

        }

        if (stack.getItem() instanceof ItemBaseModules) {
            EnumModule module = EnumModule.getFromID(stack.getItemDamage());
            if (module.type.equals(EnumBaseType.PHASE)) {
                event.getToolTip().add(Localization.translate("iu.phasemodule"));

            }
            if (module.type.equals(EnumBaseType.MOON_LINSE)) {
                event.getToolTip().add(Localization.translate("iu.moonlinse"));

            }

        }
        if (getUpgradeItem(stack) && UpgradeSystem.system.hasInMap(stack)) {
            final List<UpgradeItemInform> lst = UpgradeSystem.system.getInformation(stack);
            final int col = UpgradeSystem.system.getRemaining(stack);
            if (!lst.isEmpty()) {
                for (UpgradeItemInform upgrade : lst) {
                    event.getToolTip().add(upgrade.getName());
                }
            }
            if (col != 0) {
                event.getToolTip().add(Localization.translate("free_slot") + col + Localization.translate(
                        "free_slot1"));
            } else {
                event.getToolTip().add(Localization.translate("not_free_slot"));

            }


        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void addInfo(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();


        for (Map.Entry<ItemStack, BaseMachineRecipe> entry : IUItem.machineRecipe) {
            if (entry.getKey().isItemEqual(stack)) {
                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    event.getToolTip().add(Localization.translate("press.lshift"));
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    event.getToolTip().add(Localization.translate("clonning"));

                    final RecipeOutput output1 = entry.getValue().output;
                    for (int i = 0; i < this.name.length; i++) {
                        if (output1.metadata.getDouble(("quantitysolid_" + i)) != 0) {
                            event
                                    .getToolTip()
                                    .add(name[i] + Localization.translate(mattertype[i]) + ": " + output1.metadata.getDouble(
                                            ("quantitysolid_" + i)) + Localization.translate("matternumber"));
                        }
                    }
                }
                return;
            }
        }
    }

}
