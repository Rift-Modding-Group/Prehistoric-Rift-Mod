package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualRecipeBase;

public class TileEntitySemiManualExtruderTop extends TileEntitySemiManualTopBase {
    @Override
    public SemiManualRecipeBase getCurrentRecipe() {
        return RiftMMRecipes.getSMExtruderRecipe(this.getCurrentRecipeId());
    }
}
