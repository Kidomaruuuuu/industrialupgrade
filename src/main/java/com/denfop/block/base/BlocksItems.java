package com.denfop.block.base;

import com.denfop.Constants;
import com.denfop.fluid.Ic2Fluid;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;

public class BlocksItems {
	public static void init() {
		initFluids();

	}

	private static void initFluids() {
		registerIC2fluid("fluidNeutron", 3867955, 3000, 300, false);
		registerIC2fluid("fluidHelium", 10983500, 1000, 300, true);
		registerIC2fluid("fluidbenz", 3867955, 3000, 500, false);
		registerIC2fluid("fluiddizel", 3867955, 3000, 500, false);
		registerIC2fluid("fluidneft", 3867955, 3000, 500, false);
//
		registerIC2fluid("fluidpolyeth", 3867955, 3000, 2000, true);
		registerIC2fluid("fluidpolyprop", 3867955, 3000, 2000, true);
		registerIC2fluid("fluidoxy", 3867955, 3000, 500, false);

		registerIC2fluid("fluidhyd", 3867955, 3000, 500, false);

	}





	private static void registerIC2fluid(String internalName, int color, int density,
										 int temperature, boolean isGaseous) {
		Block block = null;
		String fluidName = internalName.substring("fluid".length()).toLowerCase(Locale.ENGLISH);
		Fluid fluid = (new Ic2Fluid(fluidName)).setDensity(density).setViscosity(3000).setLuminosity(0)
				.setTemperature(temperature).setGaseous(isGaseous);
		if (!FluidRegistry.registerFluid(fluid))
			fluid = FluidRegistry.getFluid(fluidName);
		if (!fluid.canBePlacedInWorld()) {
			BlockIC2Fluid blockIC2Fluid = new BlockIC2Fluid(internalName, fluid,temperature >= 3000 ?Material.lava : Material.water, color);
			fluid.setBlock(blockIC2Fluid);
			fluid.setUnlocalizedName(blockIC2Fluid.getUnlocalizedName());
		} else {
			block = fluid.getBlock();
		}
		fluids.put(internalName, fluid);
		fluidBlocks.put(internalName, block);
	}

	public static void onMissingMappings(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			
			String subName = mapping.name.substring("ssp".length() + 1);
			String newName = renames.get(subName);
			if (newName != null) {
				String name = Constants.MOD_NAME + newName;
				if (mapping.type == GameRegistry.Type.BLOCK) {
					mapping.remap(GameData.getBlockRegistry().getRaw(name));
					continue;
				}
				mapping.remap(GameData.getItemRegistry().getRaw(name));
				continue;
			}
			if (dropped.contains(subName))
				mapping.ignore();
		}
	}

	public static Fluid getFluid(String name) {
		return fluids.get(name);
	}

	public static Block getFluidBlock(String blockName) {
		return fluidBlocks.get(blockName);
	}

	private static final Map<String, Fluid> fluids = new HashMap<>();

	private static final Map<String, Block> fluidBlocks = new HashMap<>();

	private static final Map<String, String> renames = new HashMap<>();

	private static final Set<String> dropped = new HashSet<>();

}