package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ZenClass("mods.prift.SemiManualPresser")
public class RiftCrafttweakerSemiManualPresser {
    @ZenMethod
    public static void addRecipe(String id, IItemStack input, IItemStack output, double minPower) {
        RiftMMRecipes.smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "smp/"+id), CraftTweakerMC.getIngredient(input), CraftTweakerMC.getIngredient(output), minPower));
    }

    @ZenMethod
    public static void removeRecipeByOutput(IItemStack output) {
        RiftMMRecipes.smPresserRecipes = RiftMMRecipes.smPresserRecipes.stream()
                .filter(recipe -> !recipe.output.apply(CraftTweakerMC.getItemStack(output)))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public static void removeAllRecipes() {
        RiftMMRecipes.smPresserRecipes = new ArrayList<>();
    }
}
