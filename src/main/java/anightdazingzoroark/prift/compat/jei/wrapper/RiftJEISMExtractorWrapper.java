package anightdazingzoroark.prift.compat.jei.wrapper;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

import java.util.Arrays;

public class RiftJEISMExtractorWrapper implements IRecipeWrapper {
    private final SemiManualExtractorRecipe recipe;

    public RiftJEISMExtractorWrapper(SemiManualExtractorRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, Arrays.asList(this.recipe.input.matchingStacks));
        iIngredients.setOutput(VanillaTypes.FLUID, this.recipe.output);
    }
}
