package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualRecipeBase;

public class TileEntitySemiManualPresserTop extends TileEntitySemiManualTopBase {
    @Override
    public SemiManualRecipeBase getCurrentRecipe() {
        return RiftMMRecipes.getSMPresserRecipe(this.getCurrentRecipeId());
    }
}
