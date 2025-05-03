package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.stream.Collectors;

@ZenClass("mods.prift.SemiManualExtruder")
public class RiftCrafttweakerSemiManualExtruder {
    @ZenMethod
    public static void addRecipe(String id, IItemStack input, IItemStack output, double minPower) {
        RiftMMRecipes.smExtruderRecipes.add(new SemiManualExtruderRecipe(new ResourceLocation(RiftInitialize.MODID, "smex/"+id), CraftTweakerMC.getIngredient(input), CraftTweakerMC.getIngredient(output), minPower));
    }

    @ZenMethod
    public static void removeRecipeByOutput(IItemStack output) {
        RiftMMRecipes.smExtruderRecipes = RiftMMRecipes.smExtruderRecipes.stream()
                .filter(recipe -> !recipe.output.apply(CraftTweakerMC.getItemStack(output)))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public static void removeAllRecipes() {
        RiftMMRecipes.smExtruderRecipes = new ArrayList<>();
    }
}
