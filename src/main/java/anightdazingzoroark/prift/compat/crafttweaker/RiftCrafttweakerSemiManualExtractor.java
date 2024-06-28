package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.stream.Collectors;

@ZenClass("mods.prift.SemiManualExtractor")
public class RiftCrafttweakerSemiManualExtractor {
    @ZenMethod
    public static void addRecipe(String id, IItemStack input, ILiquidStack output, double minPower) {
        RiftMMRecipes.smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "sme/"+id), CraftTweakerMC.getIngredient(input), CraftTweakerMC.getLiquidStack(output), minPower));
    }

    @ZenMethod
    public static void removeRecipeByOutput(ILiquidStack output) {
        RiftMMRecipes.smExtractorRecipes = RiftMMRecipes.smExtractorRecipes.stream()
                .filter(recipe -> !recipe.output.containsFluid(CraftTweakerMC.getLiquidStack(output)))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public static void removeAllRecipes() {
        RiftMMRecipes.smExtractorRecipes = new ArrayList<>();
    }
}
