package anightdazingzoroark.prift.compat.jei.category;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtractorWrapper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class RiftJEISMExtractorCategory implements IRecipeCategory<RiftJEISMExtractorWrapper> {
    @Override
    public String getUid() {
        return "prift.semi_manual_extractor";
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getModName() {
        return RiftInitialize.MODID;
    }

    @Override
    public IDrawable getBackground() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, RiftJEISMExtractorWrapper riftJEISMExtractorWrapper, IIngredients iIngredients) {

    }
}
