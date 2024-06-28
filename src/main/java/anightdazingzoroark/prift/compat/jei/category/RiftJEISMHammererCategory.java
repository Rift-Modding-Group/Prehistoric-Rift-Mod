package anightdazingzoroark.prift.compat.jei.category;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMHammererWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMPresserWrapper;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RiftJEISMHammererCategory implements IRecipeCategory<RiftJEISMHammererWrapper> {
    private final IDrawable background;
    private final IDrawableAnimated animatedArrow;
    private final IDrawable icon;

    public RiftJEISMHammererCategory(IGuiHelper guiHelper) {
        IDrawableStatic staticArrow = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_presser.png"), 176, 0, 21, 14);
        this.animatedArrow = guiHelper.createAnimatedDrawable(staticArrow, 200, IDrawableAnimated.StartDirection.LEFT, false);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(RiftMMItems.SEMI_MANUAL_HAMMERER));
        this.background = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_presser.png"), 4, 4, 168, 75);
    }

    @Override
    public String getUid() {
        return RiftJEI.smHammererCat;
    }

    @Override
    public String getTitle() {
        return I18n.format("tile.semi_manual_hammerer.name");
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
        this.animatedArrow.draw(minecraft, 74, 32);
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, RiftJEISMHammererWrapper riftJEISMHammererWrapper, IIngredients iIngredients) {
        IGuiItemStackGroup stacks = iRecipeLayout.getItemStacks();
        stacks.init(0, true, 40, 31);
        stacks.init(1, false, 111, 31);
        stacks.set(iIngredients);
    }
}
