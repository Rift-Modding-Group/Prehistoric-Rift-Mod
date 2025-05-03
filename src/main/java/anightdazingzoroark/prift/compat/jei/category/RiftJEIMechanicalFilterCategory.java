package anightdazingzoroark.prift.compat.jei.category;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEIMechanicalFilterWrapper;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RiftJEIMechanicalFilterCategory implements IRecipeCategory<RiftJEIMechanicalFilterWrapper> {
    private final IDrawable background;
    private final IDrawableAnimated animatedArrow;
    private final IDrawable icon;

    public RiftJEIMechanicalFilterCategory(IGuiHelper guiHelper) {
        IDrawableStatic staticArrow = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/millstone.png"), 176, 0, 14, 22);
        this.animatedArrow = guiHelper.createAnimatedDrawable(staticArrow, 200, IDrawableAnimated.StartDirection.TOP, false);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(RiftMMBlocks.MECHANICAL_FILTER));
        this.background = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/millstone.png"), 4, 4, 168, 100);
    }

    @Override
    public String getUid() {
        return RiftJEI.mechFilterCat;
    }

    @Override
    public String getTitle() {
        return I18n.format("tile.mechanical_filter.name");
    }

    @Override
    public String getModName() {
        return RiftInitialize.MODID;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.animatedArrow.draw(minecraft, 76, 39);
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, RiftJEIMechanicalFilterWrapper riftJEIMechanicalFilterWrapper, IIngredients iIngredients) {
        IGuiItemStackGroup stacks = iRecipeLayout.getItemStacks();
        stacks.init(0, true, 75, 15);
        stacks.init(1, false, 75, 69);
        stacks.set(iIngredients);
    }
}
