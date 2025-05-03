package anightdazingzoroark.prift.compat.jei.category;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtractorWrapper;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RiftJEISMExtractorCategory implements IRecipeCategory<RiftJEISMExtractorWrapper> {
    private final IDrawable background;
    private final IDrawableAnimated animatedArrow;
    private final IDrawableStatic scaleBar;
    private final IDrawable icon;

    public RiftJEISMExtractorCategory(IGuiHelper guiHelper) {
        IDrawableStatic staticArrow = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_extractor.png"), 176, 0, 21, 14);
        this.animatedArrow = guiHelper.createAnimatedDrawable(staticArrow, 200, IDrawableAnimated.StartDirection.LEFT, false);
        this.scaleBar = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_extractor.png"), 176, 14, 34, 52);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(RiftMMItems.SEMI_MANUAL_EXTRACTOR));
        this.background = guiHelper.createDrawable(new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_extractor.png"), 4, 4, 168, 75);
    }

    @Override
    public String getUid() {
        return RiftJEI.smExtractorCat;
    }

    @Override
    public String getTitle() {
        return I18n.format("tile.semi_manual_extractor.name");
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
        this.animatedArrow.draw(minecraft, 64, 32);
        this.scaleBar.draw(minecraft, 93, 14);
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, RiftJEISMExtractorWrapper riftJEISMExtractorWrapper, IIngredients iIngredients) {
        //for items
        IGuiItemStackGroup stacks = iRecipeLayout.getItemStacks();
        stacks.init(0, true, 40, 31);
        stacks.set(iIngredients);

        //for fluids
        IGuiFluidStackGroup fluidStacks = iRecipeLayout.getFluidStacks();
        fluidStacks.init(1, false, 93, 14, 34, 52, 4000, false, null);
        fluidStacks.set(iIngredients);
    }
}
