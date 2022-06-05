package com.denfop.items.energy;


import com.denfop.Config;
import com.denfop.Constants;
import com.denfop.IUCore;
import com.denfop.api.IModelRegister;
import com.denfop.api.Recipes;
import com.denfop.api.recipe.RecipeOutput;
import com.denfop.api.upgrade.EnumUpgrades;
import com.denfop.api.upgrade.IUpgradeWithBlackList;
import com.denfop.api.upgrade.UpgradeSystem;
import com.denfop.api.upgrade.event.EventItemBlackListLoad;
import com.denfop.items.EnumInfoUpgradeModules;
import com.denfop.proxy.CommonProxy;
import com.denfop.utils.ExperienceUtils;
import com.denfop.utils.KeyboardClient;
import com.denfop.utils.ModUtils;
import com.denfop.utils.RetraceDiggingUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemAdvancedMultiTool extends ItemTool implements IElectricItem, IUpgradeWithBlackList, IModelRegister {

    public static final Set<IBlockState> mineableBlocks = Sets.newHashSet(
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.DOUBLE_STONE_SLAB.getDefaultState(),
            Blocks.STONE_SLAB.getDefaultState(),
            Blocks.STONE.getDefaultState(),
            Blocks.SANDSTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState(),
            Blocks.IRON_ORE.getDefaultState(),
            Blocks.IRON_BLOCK.getDefaultState(),
            Blocks.COAL_ORE.getDefaultState(),
            Blocks.GOLD_BLOCK.getDefaultState(),
            Blocks.GOLD_ORE.getDefaultState(),
            Blocks.DIAMOND_ORE.getDefaultState(),
            Blocks.DIAMOND_BLOCK.getDefaultState(),
            Blocks.ICE.getDefaultState(),
            Blocks.NETHERRACK.getDefaultState(),
            Blocks.LAPIS_ORE.getDefaultState(),
            Blocks.LAPIS_BLOCK.getDefaultState(),
            Blocks.REDSTONE_ORE.getDefaultState(),
            Blocks.LIT_REDSTONE_ORE.getDefaultState(),
            Blocks.RAIL.getDefaultState(),
            Blocks.DETECTOR_RAIL.getDefaultState(),
            Blocks.GOLDEN_RAIL.getDefaultState(),
            Blocks.ACTIVATOR_RAIL.getDefaultState(),
            Blocks.GRASS.getDefaultState(),
            Blocks.DIRT.getDefaultState(),
            Blocks.SAND.getDefaultState(),
            Blocks.GRAVEL.getDefaultState(),
            Blocks.SNOW_LAYER.getDefaultState(),
            Blocks.SNOW.getDefaultState(),
            Blocks.PLANKS.getDefaultState(),
            Blocks.BOOKSHELF.getDefaultState(),
            Blocks.LOG.getDefaultState(),
            Blocks.LOG2.getDefaultState(),
            Blocks.CHEST.getDefaultState(),
            Blocks.PUMPKIN_STEM.getDefaultState(),
            Blocks.LIT_PUMPKIN.getDefaultState(),
            Blocks.LEAVES.getDefaultState(),
            Blocks.LEAVES2.getDefaultState(),
            Blocks.CLAY.getDefaultState(),
            Blocks.FARMLAND.getDefaultState(),
            Blocks.SOUL_SAND.getDefaultState(),
            Blocks.MYCELIUM.getDefaultState()
    );
    private static final Set<Material> materials = Sets.newHashSet(Material.WOOD, Material.LEAVES,
            Material.CORAL, Material.CACTUS, Material.PLANTS, Material.VINE, Material.IRON, Material.ANVIL,
            Material.ROCK, Material.GRASS, Material.ICE, Material.PACKED_ICE, Material.GRASS, Material.GROUND,
            Material.SAND, Material.SNOW, Material.CRAFTED_SNOW, Material.CLAY
    );
    private static final Set<String> toolType = ImmutableSet.of("pickaxe", "shovel", "axe");
    public final String name;
    public final float energyPerultraLowPowerOperation1 = Config.energyPerultraLowPowerOperation1;
    private final float bigHolePower = Config.bigHolePower;
    private final float normalPower = Config.effPower;
    private final float ultraLowPower = Config.ultraLowPower;
    private final int maxCharge = Config.ultdrillmaxCharge;
    private final int tier = Config.ultdrilltier;
    private final int energyPerOperation = Config.energyPerOperation;
    private final int energyPerLowOperation = Config.energyPerLowOperation;
    private final int energyPerbigHolePowerOperation = Config.energyPerbigHolePowerOperation;
    private final int energyPerultraLowPowerOperation = Config.energyPerultraLowPowerOperation;
    private final int transferLimit = Config.ultdrilltransferLimit;
    private final float ultraLowPower1 = Config.ultraLowPower1;
    private final List<String> blacklist = new ArrayList<>();
    private boolean hasBlackList = false;

    public ItemAdvancedMultiTool(Item.ToolMaterial toolMaterial, String name) {
        super(0.0F, 0.0F + toolMaterial.getAttackDamage(), toolMaterial, new HashSet<>());
        setMaxDamage(27);

        setCreativeTab(IUCore.EnergyTab);
        this.name = name;
        this.efficiency = this.normalPower;
        this.setUnlocalizedName(name);
        BlocksItems.registerItem((Item) this, IUCore.getIdentifier(name)).setUnlocalizedName(name);
        IUCore.proxy.addIModelRegister(this);
        UpgradeSystem.system.addRecipe(this, EnumUpgrades.INSTRUMENTS.list);
    }


    public static int readToolMode(ItemStack itemstack) {
        NBTTagCompound nbt = ModUtils.nbt(itemstack);
        int toolMode = nbt.getInteger("toolMode");

        if (toolMode < 0 || toolMode > 5) {
            toolMode = 0;
        }
        return toolMode;
    }

    @SideOnly(Side.CLIENT)
    public static ModelResourceLocation getModelLocation1(String name, String extraName) {
        final String loc = Constants.MOD_ID +
                ':' +
                "energy_tools" + "/" + name + extraName;

        return new ModelResourceLocation(loc, null);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack itemStack, @Nonnull World world, @Nonnull Entity entity, int slot, boolean par5) {
        NBTTagCompound nbt = ModUtils.nbt(itemStack);

        if (!UpgradeSystem.system.hasInMap(itemStack)) {
            nbt.setBoolean("hasID", false);
            MinecraftForge.EVENT_BUS.post(new EventItemBlackListLoad(world, this, itemStack, itemStack.getTagCompound()));
        }
    }

    boolean break_block(
            World world, Block block, RayTraceResult mop, byte mode_item, EntityPlayer player, BlockPos pos,
            ItemStack stack
    ) {
        byte xRange = mode_item;
        byte yRange = mode_item;
        byte zRange = mode_item;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (mop.sideHit.ordinal()) {
            case 0:
            case 1:
                yRange = 0;
                break;
            case 2:
            case 3:
                zRange = 0;
                break;
            case 4:
            case 5:
                xRange = 0;
                break;
        }

        boolean lowPower = false;
        boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

        int Yy;
        Yy = yRange > 0 ? yRange - 1 : 0;
        NBTTagCompound nbt = ModUtils.nbt(stack);
        float energy = energy(stack);
        byte dig_depth = (byte) (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.DIG_DEPTH, stack) ?
                UpgradeSystem.system.getModules(EnumInfoUpgradeModules.DIG_DEPTH, stack).number : 0);


        zRange = zRange > 0 ? zRange : (byte) (zRange + dig_depth);
        xRange = xRange > 0 ? xRange : (byte) (xRange + dig_depth);
        yRange = yRange > 0 ? yRange : (byte) (yRange + dig_depth);
        boolean save = nbt.getBoolean("save");
        if (!player.capabilities.isCreativeMode) {
            for (int xPos = x - xRange; xPos <= x + xRange; xPos++) {
                for (int yPos = y - yRange + Yy; yPos <= y + yRange + Yy; yPos++) {
                    for (int zPos = z - zRange; zPos <= z + zRange; zPos++) {
                        if (ElectricItem.manager.canUse(stack, energy)) {

                            BlockPos pos_block = new BlockPos(xPos, yPos, zPos);
                            if (save) {
                                if (world.getTileEntity(pos_block) != null) {
                                    continue;
                                }
                            }

                            IBlockState state = world.getBlockState(pos_block);
                            Block localBlock = world.getBlockState(pos_block).getBlock();
                            if (localBlock.equals(Blocks.SKULL))
                                continue;
                            if (!localBlock.equals(Blocks.AIR) && canHarvestBlock(state, stack)
                                    && state.getBlockHardness(world, pos_block) >= 0.0F
                            ) {
                                if (state.getBlockHardness(world, pos_block) > 0.0F) {
                                    onBlockDestroyed(stack, world, state, pos_block,
                                            player
                                    );
                                }
                                if (!silktouch) {
                                    ExperienceUtils.addPlayerXP(player, getExpierence(state, world, pos_block, fortune, stack
                                            , localBlock));
                                }


                            } else {
                                if (state.getBlockHardness(world, pos_block) > 0.0F && materials.contains(state.getMaterial())) {
                                    return onBlockDestroyed(stack, world, state, pos_block,
                                            player
                                    );
                                }


                            }


                        } else {
                            lowPower = true;
                            break;
                        }
                    }
                }
            }
        } else {
            if (ElectricItem.manager.canUse(stack, energy)) {
                Block localBlock = world.getBlockState(pos).getBlock();
                IBlockState state = world.getBlockState(pos);
                if (localBlock.equals(Blocks.SKULL))
                    return  false;
                if (localBlock.equals(Blocks.AIR) && canHarvestBlock(state, stack)
                        && state.getBlockHardness(world, pos) >= 0.0F
                        && (materials.contains(state.getMaterial())
                        || block == Blocks.MONSTER_EGG)) {
                    if (state.getBlockHardness(world, pos) > 0.0F) {
                        onBlockDestroyed(stack, world, state, pos,
                                player
                        );
                    }
                    if (!silktouch) {
                        localBlock.dropXpOnBlockBreak(world, pos,
                                localBlock.getExpDrop(state, world, pos, fortune)
                        );
                    }


                } else {
                    if (state.getBlockHardness(world, pos) > 0.0F) {
                        return onBlockDestroyed(stack, world, state, pos,
                                player
                        );
                    }
                }
            }
        }
        if (lowPower) {
            if (ElectricItem.manager.canUse(stack, energy)) {
                IBlockState state = world.getBlockState(pos);
                Block localBlock = state.getBlock();
                if (localBlock.equals(Blocks.SKULL))
                    return  false;
                if (!localBlock.equals(Blocks.AIR) && canHarvestBlock(state, stack)
                        && state.getBlockHardness(world, pos) >= 0.0F
                        && (materials.contains(state.getMaterial())
                        || block == Blocks.MONSTER_EGG)) {

                    if (state.getBlockHardness(world, pos) > 0.0F) {
                        onBlockDestroyed(stack, world, state, pos,
                                player
                        );
                    }
                    if (!silktouch) {
                        localBlock.dropXpOnBlockBreak(world, pos,
                                localBlock.getExpDrop(state, world, pos, fortune)
                        );
                    }


                } else {
                    if (state.getBlockHardness(world, pos) > 0.0F) {
                        return onBlockDestroyed(stack, world, state, pos,
                                player
                        );
                    }
                }
            }
        }
        return true;
    }

    public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase damagee, @Nonnull EntityLivingBase damager) {
        return true;
    }

    public int getItemEnchantability() {
        return 0;
    }

    public boolean isBookEnchantable(@Nonnull ItemStack stack, @Nonnull ItemStack book) {
        return false;
    }

    public boolean canProvideEnergy(ItemStack itemStack) {
        return false;
    }

    public double getMaxCharge(ItemStack itemStack) {
        return this.maxCharge;
    }

    public int getTier(ItemStack itemStack) {
        return this.tier;
    }

    public double getTransferLimit(ItemStack itemStack) {
        return this.transferLimit;
    }

    @Nonnull
    public Set<String> getToolClasses(@Nonnull ItemStack stack) {
        return toolType;
    }

    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        int energy = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.ENERGY, stack) ?
                UpgradeSystem.system.getModules(EnumInfoUpgradeModules.ENERGY, stack).number : 0;
        int speed = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.EFFICIENCY, stack) ?
                UpgradeSystem.system.getModules(EnumInfoUpgradeModules.EFFICIENCY, stack).number : 0;
        return !ElectricItem.manager.canUse(stack, (this.energyPerOperation - (int) (this.energyPerOperation * 0.25 * energy)))
                ? 1.0F
                : (canHarvestBlock(state, stack) ? (this.efficiency + (int) (this.efficiency * 0.2 * speed)) : 1.0F);

    }

    @Override
    public int getHarvestLevel(
            @Nonnull final ItemStack stack,
            final String toolClass,
            @Nullable final EntityPlayer player,
            @Nullable final IBlockState blockState
    ) {
        return (!toolClass.equals("pickaxe") && !toolClass.equals("shovel") && !toolClass.equals("axe")) ?
                super.getHarvestLevel(stack, toolClass, player, blockState)
                : this.toolMaterial.getHarvestLevel();
    }

    @Override
    public boolean canHarvestBlock(@Nonnull final IBlockState state, @Nonnull final ItemStack stack) {
        return (Items.DIAMOND_PICKAXE.canHarvestBlock(state, stack)
                || Items.DIAMOND_PICKAXE.getDestroySpeed(stack, state) > 1.0F || mineableBlocks.contains(state) ||
                Items.DIAMOND_SHOVEL.canHarvestBlock(state, stack)
                || Items.DIAMOND_SHOVEL.getDestroySpeed(stack, state) > 1.0F
                ||
                Items.DIAMOND_AXE.canHarvestBlock(state, stack)
                || Items.DIAMOND_AXE.getDestroySpeed(stack, state) > 1.0F);

    }

    void trimLeavs(BlockPos pos, World world) {
        scedualUpdates(pos, world);
    }

    void chopTree(BlockPos pos, EntityPlayer player, World world, ItemStack stack) {
        int Y = pos.getY();
        int X = pos.getX();
        int Z = pos.getZ();
        for (int xPos = X - 1; xPos <= X + 1; xPos++) {
            for (int yPos = Y; yPos <= Y + 1; yPos++) {
                for (int zPos = Z - 1; zPos <= Z + 1; zPos++) {
                    BlockPos pos1 = new BlockPos(xPos, yPos, zPos);

                    IBlockState state = world.getBlockState(pos1);
                    Block block = state.getBlock();

                    if (block.isWood(world, pos1)) {

                        if (!player.capabilities.isCreativeMode) {
                            onBlockDestroyed(stack, world, state, pos1, player);
                        }
                        chopTree(pos1, player, world, stack);
                    }
                }
            }
        }
    }

    void scedualUpdates(BlockPos pos, World world) {

        int Y = pos.getY();
        int X = pos.getX();
        int Z = pos.getZ();
        for (int xPos = X - 15; xPos <= X + 15; xPos++) {
            for (int yPos = Y; yPos <= Y + 50; yPos++) {
                for (int zPos = Z - 15; zPos <= Z + 15; zPos++) {
                    Block block = world.getBlockState(pos).getBlock();
                    IBlockState state = world.getBlockState(pos);
                    if (block.isLeaves(state, world, pos)) {
                        world.scheduleBlockUpdate(pos, block, 2 + world.rand.nextInt(10), 0);
                    }
                }
            }
        }
    }

    private boolean isTree(World world, BlockPos pos) {
        Block wood = world.getBlockState(pos).getBlock();
        if (wood.equals(Blocks.AIR) || !wood.isWood(world, pos)) {
            return false;
        }
        int top = pos.getY();
        int Y = pos.getY();
        int X = pos.getX();
        int Z = pos.getZ();
        for (int y = pos.getY(); y <= pos.getY() + 50; y++) {
            BlockPos pos1 = new BlockPos(X, y, Z);
            if (!world.getBlockState(pos1).getBlock().isWood(world, pos1)
                    && !world.getBlockState(pos1).getBlock().isLeaves(world.getBlockState(pos1), world, pos1)) {
                top += y;
                break;
            }
        }
        int leaves = 0;
        for (int xPos = X - 1; xPos <= X + 1; xPos++) {
            for (int yPos = Y; yPos <= top; yPos++) {
                for (int zPos = Z - 1; zPos <= Z + 1; zPos++) {
                    BlockPos pos1 = new BlockPos(xPos, yPos, zPos);
                    if (world.getBlockState(pos1).getBlock().isLeaves(world.getBlockState(pos1), world, pos1)) {
                        leaves++;
                    }
                }
            }
        }
        return leaves >= 3;
    }

    public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        if (readToolMode(stack) == 0) {
            World world = player.getEntityWorld();
            Block block = world.getBlockState(pos).getBlock();
            if (block == Blocks.AIR) {
                return super.onBlockStartBreak(stack, pos, player);
            }

            RayTraceResult mop = RetraceDiggingUtils.retrace(player);
            byte aoe = (byte) (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.AOE_DIG, stack) ?
                    UpgradeSystem.system.getModules(EnumInfoUpgradeModules.AOE_DIG, stack).number : 0);

            return break_block(world, block, mop, aoe, player, pos, stack);
        }
        if (readToolMode(stack) == 1) {
            World world = player.getEntityWorld();

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.equals(Blocks.AIR)) {
                return super.onBlockStartBreak(stack, pos, player);
            }

            RayTraceResult mop = RetraceDiggingUtils.retrace(player);

            byte aoe = (byte) (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.AOE_DIG, stack) ?
                    UpgradeSystem.system.getModules(EnumInfoUpgradeModules.AOE_DIG, stack).number : 0);
                if (player.isSneaking()) {
                    return break_block(world, block, mop, aoe, player, pos, stack);
                }

                return break_block(world, block, mop, (byte) (1 + aoe), player, pos, stack);

        }
        if (readToolMode(stack) == 2) {
            World world = player.getEntityWorld();

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.equals(Blocks.AIR)) {
                return super.onBlockStartBreak(stack, pos, player);
            }
            RayTraceResult mop = RetraceDiggingUtils.retrace(player);


            byte aoe = (byte) (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.AOE_DIG, stack) ?
                    UpgradeSystem.system.getModules(EnumInfoUpgradeModules.AOE_DIG, stack).number : 0);

                if (player.isSneaking()) {
                    if (!mop.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                        return break_block(world, block, mop, aoe, player, pos, stack);
                    }
                }
                return break_block(world, block, mop, (byte) (2 + aoe), player, pos, stack);

        }
        if (readToolMode(stack) == 3) {
            World world = player.getEntityWorld();

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.equals(Blocks.AIR)) {
                return super.onBlockStartBreak(stack, pos, player);
            }

            RayTraceResult mop = RetraceDiggingUtils.retrace(player);


            byte aoe = (byte) (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.AOE_DIG, stack) ?
                    UpgradeSystem.system.getModules(EnumInfoUpgradeModules.AOE_DIG, stack).number : 0);
                 if (player.isSneaking()) {
                    if (!mop.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                        return break_block(world, block, mop, aoe, player, pos, stack);
                    }
                }
                return break_block(world, block, mop, (byte) (3 + aoe), player, pos, stack);

        }
        if (readToolMode(stack) == 5) {
            if (isTree(player.getEntityWorld(), pos)) {
                trimLeavs(pos, player.getEntityWorld());
                for (int i = 0; i < 9; i++) {
                    player.getEntityWorld().playEvent(
                            2001,
                            pos,
                            Block.getIdFromBlock(player.getEntityWorld().getBlockState(pos).getBlock())
                                    + (player
                                    .getEntityWorld()
                                    .getBlockState(pos)
                                    .getBlock()
                                    .getMetaFromState(player.getEntityWorld().getBlockState(pos)) << 12)
                    );
                }
                chopTree(pos, player, player.getEntityWorld(), stack);
            }
        }
        if (readToolMode(stack) == 4) {
            World world = player.getEntityWorld();

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.equals(Blocks.AIR)) {
                return super.onBlockStartBreak(stack, pos, player);
            }
            RayTraceResult mop = RetraceDiggingUtils.retrace(player);

            boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);


            NBTTagCompound nbt = ModUtils.nbt(stack);
            nbt.setInteger("ore", 1);

            if (!mop.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                ore_break(world, pos, player, silktouch, fortune, false, stack, block);
            }

        }
        return player.getEntityWorld().getBlockState(pos).getBlock().equals(Blocks.SKULL) || super.onBlockStartBreak(
                stack,
                pos,
                player
        );
    }

    private void ore_break(
            World world, BlockPos pos, EntityPlayer player, boolean silktouch, int fortune, boolean lowPower,
            ItemStack stack, Block block1
    ) {
        NBTTagCompound nbt = ModUtils.nbt(stack);
        int energy = 0;
        for (int i = 0; i < 4; i++) {
            if (nbt.getString("mode_module" + i).equals("energy")) {
                energy++;
            }
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        energy = Math.min(energy, EnumInfoUpgradeModules.ENERGY.max);
        for (int Xx = x - 1; Xx <= x + 1; Xx++) {
            for (int Yy = y - 1; Yy <= y + 1; Yy++) {
                for (int Zz = z - 1; Zz <= z + 1; Zz++) {
                    NBTTagCompound NBTTagCompound = ModUtils.nbt(stack);
                    int ore = NBTTagCompound.getInteger("ore");
                    if (ore < 16) {
                        if (ElectricItem.manager.canUse(
                                stack,
                                (this.energyPerOperation - this.energyPerOperation * 0.25 * energy)
                        )) {
                            BlockPos pos_block = new BlockPos(Xx, Yy, Zz);
                            Block localBlock = world.getBlockState(pos_block).getBlock();
                            IBlockState state = world.getBlockState(pos_block);
                            if (ModUtils.getore(localBlock, block1)) {


                                if (!player.capabilities.isCreativeMode) {

                                    if (state.getBlockHardness(world, pos_block) > 0.0F) {
                                        onBlockDestroyed(stack, world, state, pos_block,
                                                player
                                        );

                                    }
                                    if (!silktouch) {
                                        ExperienceUtils.addPlayerXP(player, getExpierence(state, world, pos_block, fortune, stack
                                                , localBlock));
                                    }


                                    ore = ore + 1;
                                    NBTTagCompound.setInteger("ore", ore);
                                    ore_break(world, pos_block, player, silktouch, fortune, lowPower, stack, block1);
                                } else {
                                    break;
                                }

                                world.markBlockRangeForRenderUpdate(pos_block, pos_block);

                            }
                        } else {
                            lowPower = true;
                            break;
                        }
                    }
                }
            }
        }

    }

    private int getExpierence(
            IBlockState state,
            World world,
            BlockPos pos_block,
            int fortune,
            ItemStack stack,
            final Block localBlock
    ) {
        int col = localBlock.getExpDrop(state, world, pos_block, fortune);
        col *= (UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.EXPERIENCE, stack) ?
                UpgradeSystem.system.getModules(EnumInfoUpgradeModules.EXPERIENCE, stack).number * 0.5 + 1 : 1);
        return col;
    }

    public boolean onBlockDestroyed(
            @Nonnull ItemStack stack,
            @Nonnull World world,
            IBlockState state,
            @Nonnull BlockPos pos,
            @Nonnull EntityLivingBase entity
    ) {

        Block block = state.getBlock();
        if (block.equals(Blocks.AIR)) {
            return false;
        } else {

            if (world.isAirBlock(pos)) {
                return false;
            }
            if (state.getMaterial() instanceof MaterialLiquid || (state.getBlockHardness(
                    world,
                    pos
            ) == -1 && !((EntityPlayer) entity).capabilities.isCreativeMode)) {
                return false;
            }

            if (!world.isRemote) {
                if (ForgeHooks.onBlockBreakEvent(world, world.getWorldInfo().getGameType(), (EntityPlayerMP) entity, pos) == -1) {
                    return false;
                }

                block.onBlockHarvested(world, pos, state, (EntityPlayerMP) entity);

                if (block.removedByPlayer(state, world, pos, (EntityPlayerMP) entity, true)) {
                    block.onBlockDestroyedByPlayer(world, pos, state);
                    block.harvestBlock(world, (EntityPlayerMP) entity, pos, state, null, stack);
                    NBTTagCompound nbt = ModUtils.nbt(stack);
                    List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(
                            EntityItem.class,
                            new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1,
                                    pos.getY() + 1,
                                    pos.getZ() + 1
                            )
                    );
                    boolean smelter = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.SMELTER, stack);
                    boolean comb = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.COMB_MACERATOR, stack);
                    boolean mac = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.MACERATOR, stack);
                    boolean generator = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.GENERATOR, stack);


                    ((EntityPlayerMP) entity).addExhaustion(-0.025F);
                    if ((ModUtils.getore(block, block.getMetaFromState(state)) && check_list(block, block.getMetaFromState(state)
                            , stack)) || (!Config.blacklist) || !nbt.getBoolean("black")) {
                        for (EntityItem item : items) {
                            if (!entity.getEntityWorld().isRemote) {
                                ItemStack stack1 = item.getItem();

                                if (comb) {
                                    RecipeOutput rec = Recipes.recipes.getRecipeOutput("comb_macerator", false, stack1).output;
                                    if (rec != null) {
                                        stack1 = rec.items.get(0);
                                    }
                                } else if (mac) {
                                    RecipeOutput rec = Recipes.recipes.getRecipeOutput("macerator", false, stack1).output;
                                    if (rec != null) {
                                        stack1 = rec.items.get(0);
                                    }
                                }
                                ItemStack smelt = new ItemStack(Items.AIR);
                                if (smelter) {
                                    smelt = FurnaceRecipes.instance().getSmeltingResult(stack1);
                                    if (!smelt.isEmpty()) {
                                        smelt.setCount(stack1.getCount());
                                    }
                                }
                                if (generator) {
                                    final boolean rec = Info.itemInfo.getFuelValue(stack1, false) > 0;
                                    if (rec) {
                                        int amount = stack1.getCount();
                                        int value = Info.itemInfo.getFuelValue(stack1, false) / 4;
                                        amount *= value;
                                        amount *= Math.round(10.0F * ConfigUtil.getFloat(
                                                MainConfig.get(),
                                                "balance/energy/generator/generator"
                                        ));
                                        double sentPacket = ElectricItem.manager.charge(
                                                stack,
                                                amount,
                                                2147483647,
                                                true,
                                                false
                                        );
                                        amount -= sentPacket;
                                        amount /= (value * Math.round(10.0F * ConfigUtil.getFloat(MainConfig.get(), "balance" +
                                                "/energy/generator/generator")));
                                        stack1.setCount(amount);
                                    }
                                }
                                if (!smelt.isEmpty()) {
                                    item.setItem(smelt);
                                } else {
                                    item.setItem(stack1);
                                }

                                item.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, 0.0F, 0.0F);
                                ((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityTeleport(item));
                                item.setPickupDelay(0);

                            }
                        }
                    } else {
                        if (nbt.getBoolean("black")) {
                            for (EntityItem item : items) {
                                if (!entity.getEntityWorld().isRemote) {
                                    item.setDead();

                                }
                            }
                        }
                    }
                }
                int random = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.RANDOM, stack) ?
                        UpgradeSystem.system.getModules(EnumInfoUpgradeModules.RANDOM, stack).number : 0;
                if (random != 0) {
                    final int rand = world.rand.nextInt(100001);
                    if (rand >= 100000 - random) {
                        EntityItem item = new EntityItem(world);
                        item.setItem(IUCore.get_ingot.get(world.rand.nextInt(IUCore.get_ingot.size())));
                        item.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, 0.0F, 0.0F);
                        ((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityTeleport(item));
                        item.setPickupDelay(0);
                    }
                }
                EntityPlayerMP mpPlayer = (EntityPlayerMP) entity;
                mpPlayer.connection.sendPacket(new SPacketBlockChange(world, new BlockPos(pos)));
            } else {
                if (block.removedByPlayer(state, world, pos, (EntityPlayer) entity, true)) {
                    block.onBlockDestroyedByPlayer(world, pos, state);
                }

                Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        pos,
                        Minecraft.getMinecraft().objectMouseOver.sideHit
                ));
            }
            if (entity.isEntityAlive()) {
                float energy = energy(stack);
                if (energy != 0.0F && state.getBlockHardness(world, pos) != 0.0F) {
                    ElectricItem.manager.use(stack, energy, null);
                }
            }

            return true;
        }
    }

    public boolean check_list(Block block, int metaFromState, ItemStack stack) {

        if (!UpgradeSystem.system.hasBlackList(stack)) {
            return true;
        }

        ItemStack stack1 = new ItemStack(block, 1, metaFromState);
        if (stack1.isEmpty()) {
            return true;
        }
        if (OreDictionary.getOreIDs(stack1).length < 1) {
            return true;
        }

        String name = OreDictionary.getOreName(OreDictionary.getOreIDs(stack1)[0]);


        return !UpgradeSystem.system.getBlackList(stack).contains(name);
    }

    @Override
    public List<String> getBlackList() {
        return this.blacklist;
    }

    @Override
    public void setBlackList(final boolean set) {
        this.hasBlackList = set;
    }

    @Override
    public boolean haveBlackList() {
        return this.hasBlackList;
    }

    public float energy(ItemStack stack) {
        int energy1 = UpgradeSystem.system.hasModules(EnumInfoUpgradeModules.ENERGY, stack) ?
                UpgradeSystem.system.getModules(EnumInfoUpgradeModules.ENERGY, stack).number : 0;
        int toolMode = readToolMode(stack);
        float energy;
        switch (toolMode) {
            case 1:
                energy = (float) (this.energyPerLowOperation - this.energyPerLowOperation * 0.25 * energy1);
                break;
            case 2:
                energy = (float) (this.energyPerbigHolePowerOperation - this.energyPerbigHolePowerOperation * 0.25 * energy1);
                break;
            case 3:
                energy = (float) (this.energyPerultraLowPowerOperation - this.energyPerultraLowPowerOperation * 0.25 * energy1);
                break;
            case 4:
                energy = (float) (this.energyPerultraLowPowerOperation1 - this.energyPerultraLowPowerOperation1 * 0.25 * energy1);

                break;
            default:
                energy = (float) (this.energyPerOperation - this.energyPerOperation * 0.25 * energy1);
                break;
        }
        return energy;


    }

    public void saveToolMode(ItemStack itemstack, int toolMode) {
        NBTTagCompound nbt = ModUtils.nbt(itemstack);
        nbt.setInteger("toolMode", toolMode);
        itemstack.setTagCompound(nbt);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(
            final EntityPlayer player,
            @Nonnull final World world,
            @Nonnull final BlockPos pos,
            @Nonnull final EnumHand hand,
            @Nonnull final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ
    ) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack torchStack = player.inventory.mainInventory.get(i);
            if (!torchStack.isEmpty() && torchStack.getUnlocalizedName().toLowerCase().contains("torch")) {
                Item item = torchStack.getItem();
                if (item instanceof net.minecraft.item.ItemBlock) {
                    int oldMeta = torchStack.getItemDamage();
                    int oldSize = torchStack.stackSize;
                    ItemStack stack = player.getHeldItem(hand).copy();
                    boolean result = torchStack.onItemUse(player, world, pos, hand, facing, hitX,
                            hitY, hitZ
                    ) == EnumActionResult.SUCCESS;
                    if (player.capabilities.isCreativeMode) {
                        torchStack.setItemDamage(oldMeta);
                        torchStack.stackSize = oldSize;
                    }
                    if (result) {
                        ForgeEventFactory.onPlayerDestroyItem(player, torchStack, null);
                        torchStack = player.inventory.mainInventory.get(i);
                        player.setHeldItem(hand, stack);
                        torchStack.setCount(torchStack.getCount() - 1);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(
            @Nonnull final World worldIn,
            final EntityPlayer player,
            @Nonnull final EnumHand hand
    ) {

        ItemStack itemStack = player.getHeldItem(hand);
        if (IUCore.keyboard.isSaveModeKeyDown(player)) {
            NBTTagCompound nbt = ModUtils.nbt(itemStack);
            boolean save = !nbt.getBoolean("save");
            CommonProxy.sendPlayerMessage(
                    player,
                    TextFormatting.GREEN + Localization.translate("message.savemode") +
                            (save ? Localization.translate("message.allow") : Localization.translate("message.disallow"))
            );
            nbt.setBoolean("save", save);
        }
        if (IUCore.keyboard.isBlackListModeKeyDown(player)) {
            NBTTagCompound nbt = ModUtils.nbt(itemStack);
            boolean black = !nbt.getBoolean("black");
            CommonProxy.sendPlayerMessage(
                    player,
                    TextFormatting.GREEN + Localization.translate("message.blacklist") +
                            (black ? Localization.translate("message.allow") : Localization.translate("message.disallow"))
            );
            nbt.setBoolean("black", black);
        }
        if (IUCore.keyboard.isChangeKeyDown(player)) {
            int toolMode = readToolMode(itemStack) + 1;
            if (!IC2.platform.isRendering()) {
                IUCore.audioManager.playOnce(
                        player,
                        com.denfop.audio.PositionSpec.Hand,
                        "Tools/toolChange.ogg",
                        true,
                        IC2.audioManager.getDefaultVolume()
                );
            }
            if (toolMode > 5) {
                toolMode = 0;
            }
            saveToolMode(itemStack, toolMode);
            switch (toolMode) {
                case 0:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.GREEN + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.normal")
                        );
                    }
                    this.efficiency = this.normalPower;
                    break;


                case 1:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.AQUA + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.bigHoles")
                        );
                    }
                    this.efficiency = this.bigHolePower;
                    break;
                case 2:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.LIGHT_PURPLE + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.bigHoles1")
                        );
                    }
                    this.efficiency = this.ultraLowPower;
                    break;
                case 3:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.DARK_PURPLE + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.bigHoles2")
                        );
                    }
                    this.efficiency = this.ultraLowPower1;

                    break;
                case 4:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.GREEN + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.pickaxe")
                        );
                    }
                    this.efficiency = this.normalPower;
                    break;
                case 5:
                    if (IC2.platform.isSimulating()) {
                        IC2.platform.messagePlayer(
                                player,
                                TextFormatting.GREEN + Localization.translate("message.text.mode") + ": "
                                        + Localization.translate("message.ultDDrill.mode.treemode")
                        );
                    }
                    this.efficiency = this.normalPower;

                    break;
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(
            @Nonnull final ItemStack par1ItemStack,
            @Nullable final World worldIn,
            @Nonnull final List<String> par3List,
            @Nonnull final ITooltipFlag flagIn
    ) {
        int toolMode = readToolMode(par1ItemStack);
        switch (toolMode) {
            case 0:


                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.normal"));
                par3List.add(Localization.translate("message.description.normal"));
                break;
            case 1:
                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.bigHoles"));
                par3List.add(Localization.translate("message.description.bigHoles"));

                break;
            case 2:

                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.bigHoles1"));
                par3List.add(Localization.translate("message.description.bigHoles1"));
                break;
            case 3:
                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.bigHoles2"));
                par3List.add(Localization.translate("message.description.bigHoles2"));
                break;
            case 4:

                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.pickaxe"));
                par3List.add(Localization.translate("message.description.pickaxe"));
                break;
            case 5:
                par3List.add(TextFormatting.GOLD + Localization.translate("message.text.mode") + ": "
                        + TextFormatting.WHITE + Localization.translate("message.ultDDrill.mode.treemode"));
                par3List.add(Localization.translate("message.description.treemode"));
                break;
        }
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            par3List.add(Localization.translate("press.lshift"));
        }


        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            par3List.add(Localization.translate("iu.changemode_key") + Keyboard.getKeyName(KeyboardClient.changemode.getKeyCode()) + Localization.translate(
                    "iu.changemode_rcm"));

            par3List.add(Localization.translate("iu.blacklist_key") + Keyboard.getKeyName(KeyboardClient.blackmode.getKeyCode()) + Localization.translate(
                    "iu.changemode_rcm"));

        }
        super.addInformation(par1ItemStack, worldIn, par3List, flagIn);
    }

    @Override
    public void getSubItems(@Nonnull final CreativeTabs subs, @Nonnull final NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(subs)) {
            ItemStack stack = new ItemStack(this, 1);

            NBTTagCompound nbt = ModUtils.nbt(stack);
            ElectricItem.manager.charge(stack, 2.147483647E9D, 2147483647, true, false);
            nbt.setInteger("ID_Item", Integer.MAX_VALUE);
            items.add(stack);
            ItemStack itemstack = new ItemStack(this, 1, 27);
            nbt = ModUtils.nbt(itemstack);
            nbt.setInteger("ID_Item", Integer.MAX_VALUE);
            items.add(itemstack);
        }
    }

    @Override
    public void registerModels() {
        registerModels(this.name);
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(final String name) {
        ModelLoader.setCustomMeshDefinition(this, stack -> {
            final NBTTagCompound nbt = ModUtils.nbt(stack);

            return getModelLocation1(name, nbt.getString("mode"));
        });
        String[] mode = {"", "Demon", "Dark", "Cold", "Ender"};
        for (final String s : mode) {
            ModelBakery.registerItemVariants(this, getModelLocation1(name, s));
        }

    }

    @Override
    public void setUpdate(final boolean update) {
    }


}
