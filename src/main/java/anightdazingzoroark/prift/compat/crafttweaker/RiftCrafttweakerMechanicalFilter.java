package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.MechanicalFilterRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ZenClass("mods.prift.MechanicalFilter")
public class RiftCrafttweakerMechanicalFilter {
    @ZenMethod
    public static void addRecipe(String id, IItemStack input, IItemStack[] output, int[] weight, double minPower) {
        if (output.length == weight.length) {
            List<MechanicalFilterRecipe.MechanicalFilterOutput> outputList = new ArrayList<>();
            for (int x = 0; x < output.length; x++) {
                outputList.add(new MechanicalFilterRecipe.MechanicalFilterOutput(CraftTweakerMC.getIngredient(output[x]), weight[x]));
            }
            RiftMMRecipes.mechanicalFilterRecipes.add(new MechanicalFilterRecipe(new ResourceLocation(RiftInitialize.MODID, "mechanicalFilter"+id), CraftTweakerMC.getIngredient(input), outputList, minPower));
        }
    }

    @ZenMethod
    public static void addOutputToRecipe(IItemStack input, IItemStack newOutput, int weight) {
        RiftMMRecipes.mechanicalFilterRecipes.stream()
                .filter(recipe -> recipe.input.apply(CraftTweakerMC.getItemStack(input)))
                .forEach(recipe -> recipe.output.add(new MechanicalFilterRecipe.MechanicalFilterOutput(CraftTweakerMC.getIngredient(newOutput), weight)));
    }

    @ZenMethod
    public static void removeRecipeByInput(IItemStack input) {
        RiftMMRecipes.millstoneRecipes = RiftMMRecipes.millstoneRecipes.stream()
                .filter(recipe -> !recipe.input.apply(CraftTweakerMC.getItemStack(input)))
                .collect(Collectors.toList());
    }

    @ZenMethod
    public static void removeAllRecipes() {
        RiftMMRecipes.mechanicalFilterRecipes = new ArrayList<>();
    }
}
