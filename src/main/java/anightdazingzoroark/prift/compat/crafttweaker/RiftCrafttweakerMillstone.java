package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.MillstoneRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.stream.Collectors;

@ZenClass("mods.prift.Millstone")
public class RiftCrafttweakerMillstone {
    @ZenMethod
    public static void addRecipe(String id, IItemStack input, IItemStack output, double minPower) {
        RiftMMRecipes.millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/"+id), CraftTweakerMC.getIngredient(input), CraftTweakerMC.getIngredient(output), minPower));
    }

    @ZenMethod
    public static void removeRecipeByOutput(IItemStack output) {
        RiftMMRecipes.millstoneRecipes = RiftMMRecipes.millstoneRecipes.stream()
                .filter(recipe -> !recipe.output.apply(CraftTweakerMC.getItemStack(output)))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public static void removeAllRecipes() {
        RiftMMRecipes.millstoneRecipes = new ArrayList<>();
    }
}
