package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class RiftMMRecipes {
    public static ArrayList<SemiManualExtractorRecipe> smExtractorRecipes = new ArrayList<>();

    public static SemiManualExtractorRecipe getSMExtractorRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static void registerRecipes() {
        //for semi manual extractor
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "cobbleToLava"), Ingredient.fromStacks(new ItemStack(Blocks.COBBLESTONE)), new FluidStack(FluidRegistry.LAVA, 1000), 10));
    }
}
