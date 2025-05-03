package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class MillstoneRecipe {
    private final ResourceLocation id;
    public final Ingredient input;
    public final Ingredient output;
    private final double minPower;

    public MillstoneRecipe(ResourceLocation id, Ingredient input, Ingredient output, double minPower) {
        this.id = id;
        this.input = input;
        this.output = output;
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
