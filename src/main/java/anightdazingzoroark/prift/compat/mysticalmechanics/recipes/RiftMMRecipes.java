package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class RiftMMRecipes {
    public static ArrayList<SemiManualExtractorRecipe> smExtractorRecipes = new ArrayList<>();
    public static ArrayList<SemiManualPresserRecipe> smPresserRecipes = new ArrayList<>();
    public static ArrayList<SemiManualExtruderRecipe> smExtruderRecipes = new ArrayList<>();

    public static SemiManualRecipeBase getSMRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        for (SemiManualPresserRecipe recipe : smPresserRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        for (SemiManualExtruderRecipe recipe : smExtruderRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualExtractorRecipe getSMExtractorRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualPresserRecipe getSMPresserRecipe(String path) {
        for (SemiManualPresserRecipe recipe : smPresserRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualExtruderRecipe getSMExtruderRecipe(String path) {
        for (SemiManualExtruderRecipe recipe : smExtruderRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static void registerRecipes() {
        //for semi manual extractor
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "sme/pyroberryToPyroberryJuice"), Ingredient.fromStacks(new ItemStack(RiftItems.PYROBERRY)), new FluidStack(RiftFluids.PYROBERRY_JUICE, 125), 10));
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "sme/cryoberryToCryoberryJuice"), Ingredient.fromStacks(new ItemStack(RiftItems.CRYOBERRY)), new FluidStack(RiftFluids.CRYOBERRY_JUICE, 125), 10));

        //for semi manual presser
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "smp/ironIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.IRON_PLATE)), 20));
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "smp/goldIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.GOLD_PLATE)), 20));

        //for semi manual extruder
        smExtruderRecipes.add(new SemiManualExtruderRecipe(new ResourceLocation(RiftInitialize.MODID, "smex/ironIngotToRod"), Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.IRON_ROD, 2)), 20));
        smExtruderRecipes.add(new SemiManualExtruderRecipe(new ResourceLocation(RiftInitialize.MODID, "smex/goldIngotToRod"), Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.GOLD_ROD, 2)), 20));
    }
}
