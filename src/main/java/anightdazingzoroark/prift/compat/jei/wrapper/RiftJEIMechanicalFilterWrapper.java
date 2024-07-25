package anightdazingzoroark.prift.compat.jei.wrapper;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.MechanicalFilterRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.crafting.Ingredient;

import java.awt.*;
import java.util.Arrays;

public class RiftJEIMechanicalFilterWrapper implements IRecipeWrapper {
    private final MechanicalFilterRecipe recipe;
    private final MechanicalFilterRecipe.MechanicalFilterOutput output;

    public RiftJEIMechanicalFilterWrapper(MechanicalFilterRecipe recipe, MechanicalFilterRecipe.MechanicalFilterOutput output) {
        this.recipe = recipe;
        this.output = output;
    }

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, Arrays.asList(this.recipe.input.matchingStacks));
        iIngredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(this.output.getOutput().matchingStacks));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String minPower = I18n.format("jei.min_power", this.recipe.getMinPower());
        int mPowerStringWidth = minecraft.fontRenderer.getStringWidth(minPower);
        int mPowerStringHeight = minecraft.fontRenderer.FONT_HEIGHT;

        double totalWeight = this.recipe.output.stream()
                .mapToInt(MechanicalFilterRecipe.MechanicalFilterOutput::getWeight)
                .sum();
        double chanceNum = this.output.getWeight() / totalWeight * 100;
        double chanceRounded = (double) Math.round(chanceNum * 100) / 100;
        String chance = I18n.format("jei.chance", chanceRounded+" %");
        int chanceStringWidth = minecraft.fontRenderer.getStringWidth(chance);
        int chanceStringHeight = minecraft.fontRenderer.FONT_HEIGHT;

        minecraft.fontRenderer.drawString(minPower, (recipeWidth - mPowerStringWidth) / 2 - 32, (recipeHeight - mPowerStringHeight) / 2 + 46, Color.BLACK.getRGB());
        minecraft.fontRenderer.drawString(chance, (recipeWidth - chanceStringWidth) / 2 + 44, (recipeHeight - chanceStringHeight) / 2, Color.BLACK.getRGB());
    }
}
