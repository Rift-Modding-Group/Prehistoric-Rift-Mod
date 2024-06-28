package anightdazingzoroark.prift.compat.jei.wrapper;

import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class RiftJEISMHammererWrapper implements IRecipeWrapper {
    private final BloomeryRecipeBase<?> recipe;

    public RiftJEISMHammererWrapper(BloomeryRecipeBase<?> recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInput(VanillaTypes.ITEM, recipe.getOutputBloom());
        ItemStack newOutput = recipe.getOutput().copy();
        newOutput.setCount(16);
        iIngredients.setOutput(VanillaTypes.ITEM, newOutput);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        double minPower = 0;
        if (this.recipe instanceof BloomeryRecipe) minPower = 20D;
        if (this.recipe instanceof WitherForgeRecipe) minPower = 40D;

        String minPowerStr = I18n.format("jei.min_power", minPower);
        int stringWidth = minecraft.fontRenderer.getStringWidth(minPowerStr);
        int stringHeight = minecraft.fontRenderer.FONT_HEIGHT;
        minecraft.fontRenderer.drawString(minPowerStr, (recipeWidth - stringWidth) / 2 - 32, (recipeHeight - stringHeight) / 2 + 36, Color.BLACK.getRGB());
    }
}
