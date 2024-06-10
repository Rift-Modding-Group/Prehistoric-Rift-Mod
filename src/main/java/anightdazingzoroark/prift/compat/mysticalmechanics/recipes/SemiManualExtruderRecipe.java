package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class SemiManualExtruderRecipe extends SemiManualRecipeBase {
    public final Ingredient output;

    public SemiManualExtruderRecipe(ResourceLocation id, Ingredient input, Ingredient output, double minPower) {
        super(input, id, minPower);
        this.output = output;
    }
}
