package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SemiManualExtractorRecipe extends SemiManualRecipeBase {
    public final FluidStack output;

    public SemiManualExtractorRecipe(ResourceLocation id, Ingredient input, FluidStack output, double minPower) {
        super(input, id, minPower);
        this.output = output;
    }
}
