package anightdazingzoroark.prift.compat.jei.wrapper;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Arrays;

public class RiftJEISMExtruderWrapper implements IRecipeWrapper {
    private final SemiManualExtruderRecipe recipe;

    public RiftJEISMExtruderWrapper(SemiManualExtruderRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.input.matchingStacks));
        iIngredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.output.matchingStacks));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String minPower = I18n.format("jei.min_power", this.recipe.getMinPower());
        int stringWidth = minecraft.fontRenderer.getStringWidth(minPower);
        int stringHeight = minecraft.fontRenderer.FONT_HEIGHT;
        minecraft.fontRenderer.drawString(minPower, (recipeWidth - stringWidth) / 2 - 32, (recipeHeight - stringHeight) / 2 + 36, Color.BLACK.getRGB());
    }
}
