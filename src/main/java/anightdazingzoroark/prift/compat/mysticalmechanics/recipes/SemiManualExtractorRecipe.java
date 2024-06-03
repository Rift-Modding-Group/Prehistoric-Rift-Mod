package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SemiManualExtractorRecipe extends SemiManualRecipeBase {
    private final List<Ingredient> inputs = new ArrayList<>();
    private final FluidStack output;

    public SemiManualExtractorRecipe(ResourceLocation id, Collection<Ingredient> inputs, FluidStack output, double minPower, double time) {
        super(id, minPower, time);
        this.inputs.addAll(inputs);
        this.output = output;
    }
}
