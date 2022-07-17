package com.denfop.recipes;

import com.denfop.IUItem;
import com.denfop.Ic2Items;
import com.denfop.register.RegisterOreDictionary;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class CompressorRecipe {

    public static final String[] recipe = {"plate", "smalldust", "verysmalldust"};
    public static final String[] recipe1 = {"doubleplate", "dust", "smalldust"};

    public static void recipe() {

        addcompressor(new ItemStack(IUItem.sunnarium, 1, 3), new ItemStack(IUItem.sunnarium, 1, 2));
        addcompressor(new ItemStack(IUItem.Helium), new ItemStack(IUItem.cell_all, 1, 2));
        addcompressor(Ic2Items.carbonPlate, 9, new ItemStack(IUItem.compresscarbon));
        addcompressor(Ic2Items.advancedAlloy, 9, new ItemStack(IUItem.compresscarbonultra));
        addcompressor(Ic2Items.iridiumPlate, 4, new ItemStack(IUItem.compressIridiumplate));
        addcompressor(new ItemStack(IUItem.cell_all, 1, 1), 1, new ItemStack(IUItem.neutronium));
        addcompressor(new ItemStack(IUItem.compressIridiumplate), 9, new ItemStack(IUItem.doublecompressIridiumplate));
        addcompressor("doubleplateTungsten", 1, new ItemStack(IUItem.cell_all));
        addcompressor(new ItemStack(IUItem.neutronium), 9, new ItemStack(IUItem.neutroniumingot, 1));
        addcompressor(new ItemStack(Ic2Items.cell.getItem()), 1, Ic2Items.airCell);

        for (int i = 0; i < RegisterOreDictionary.itemNames().size(); i++) {

            addcompressor(
                    "block" + RegisterOreDictionary.itemNames().get(i),
                    1,
                    "doubleplate" + RegisterOreDictionary.itemNames().get(i)
            );


        }
        for (int i = 0; i < RegisterOreDictionary.itemNames1().size(); i++) {

            addcompressor(
                    "block" + RegisterOreDictionary.itemNames1().get(i),
                    1,
                    "doubleplate" + RegisterOreDictionary.itemNames1().get(i)
            );


        }
        for (int j = 0; j < recipe.length; j++) {
            for (int i = 0; i < RegisterOreDictionary.itemNames().size(); i++) {

                addcompressor(
                        recipe[j] + RegisterOreDictionary.itemNames().get(i),
                        9,
                        recipe1[j] + RegisterOreDictionary.itemNames().get(i)
                );

            }
        }
        for (int j = 0; j < recipe.length; j++) {
            for (int i = 0; i < RegisterOreDictionary.itemNames1().size(); i++) {
                if (j == 0) {
                    addcompressor(
                            recipe[j] + RegisterOreDictionary.itemNames1().get(i),
                            9,
                            recipe1[j] + RegisterOreDictionary.itemNames1().get(i)
                    );
                }

            }
        }
        addcompressor(Ic2Items.uraniumOre, 1, new ItemStack(IUItem.itemiu, 1, 2));
        addcompressor(Ic2Items.crushedUraniumOre, 1, new ItemStack(IUItem.itemiu, 1, 2));
    }

    public static void addcompressor(ItemStack input, int n, ItemStack output) {

        final IRecipeInputFactory input1 = Recipes.inputFactory;
        Recipes.compressor.addRecipe(input1.forStack(input, n), null, false,
                output
        );
    }

    public static void addcompressor(String input, int n, ItemStack output) {
        final IRecipeInputFactory input1 = Recipes.inputFactory;
        Recipes.compressor.addRecipe(input1.forOreDict(input, n), null, false,
                output
        );
    }

    public static void addcompressor(String input, int n, String output) {
        final IRecipeInputFactory input1 = Recipes.inputFactory;
        Recipes.compressor.addRecipe(input1.forOreDict(input, n), null, false,
                OreDictionary.getOres(output).get(0)
        );
    }

    public static void addcompressor(ItemStack input, ItemStack output) {
        final IRecipeInputFactory input1 = Recipes.inputFactory;
        Recipes.compressor.addRecipe(input1.forStack(input, 1), null, false,
                output
        );
    }

}
