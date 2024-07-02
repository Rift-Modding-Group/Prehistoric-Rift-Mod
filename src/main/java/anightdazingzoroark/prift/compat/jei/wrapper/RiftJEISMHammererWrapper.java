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
import scala.actors.threadpool.Arrays;

import java.awt.*;

public class RiftJEISMHammererWrapper implements IRecipeWrapper {
    private final BloomeryRecipeBase<?> recipe;

    public RiftJEISMHammererWrapper(BloomeryRecipeBase<?> recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInput(VanillaTypes.ITEM, this.recipe.getOutputBloom());
        ItemStack newOutput = this.recipe.getOutput().copy();
        newOutput.setCount(this.recipe instanceof BloomeryRecipe ? 12 : 36);
        ItemStack newSlagOutput = this.recipe.getSlagItemStack().copy();
        newSlagOutput.setCount(this.recipe instanceof BloomeryRecipe ? 2 : 6);
        ItemStack failItem = this.recipe.getFailureItems()[0].getItemStack();
        failItem.setCount(this.recipe instanceof BloomeryRecipe ? 2 : 6);
        iIngredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(new ItemStack[]{newOutput, newSlagOutput, failItem}));
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
