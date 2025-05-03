package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public abstract class SemiManualRecipeBase {
    private final ResourceLocation id;
    private final double minPower;
    public final Ingredient input;

    public SemiManualRecipeBase(Ingredient input, ResourceLocation id, double minPower) {
        this.input = input;
        this.id = id;
        this.minPower = minPower;
    }

    public String getId() {
        return id.toString();
    }

    public double getMinPower() {
        return this.minPower;
    }

    public boolean matches(double powerIn, ItemStack itemIn) {
        return powerIn >= this.getMinPower() && this.input.apply(itemIn);
    }
}
