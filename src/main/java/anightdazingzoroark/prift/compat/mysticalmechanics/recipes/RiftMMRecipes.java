package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class RiftMMRecipes {
    public static ArrayList<SemiManualExtractorRecipe> smExtractorRecipes = new ArrayList<>();
    public static ArrayList<SemiManualPresserRecipe> smPresserRecipes = new ArrayList<>();

    public static SemiManualRecipeBase getSMRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        for (SemiManualPresserRecipe recipe : smPresserRecipes) {
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

    public static void registerRecipes() {
        //for semi manual extractor
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "cobbleToLava"), Ingredient.fromStacks(new ItemStack(Blocks.COBBLESTONE)), new FluidStack(FluidRegistry.LAVA, 1000), 10));

        //for semi manual presser
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "ironIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.IRON_PLATE)), 20));
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "goldIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.GOLD_PLATE)), 20));
    }
}
