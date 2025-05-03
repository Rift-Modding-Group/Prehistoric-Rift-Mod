package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MechanicalFilterRecipe {
    private final ResourceLocation id;
    public final Ingredient input;
    public final List<MechanicalFilterOutput> output;
    private final double minPower;

    public MechanicalFilterRecipe(ResourceLocation id, Ingredient input, List<MechanicalFilterOutput> output, double minPower) {
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

    public static class MechanicalFilterOutput {
        private final Ingredient output;
        private final int weight;

        public MechanicalFilterOutput(Ingredient output, int weight) {
            this.output = output;
            this.weight = weight;
        }

        public Ingredient getOutput() {
            return this.output;
        }

        public int getWeight() {
            return this.weight;
        }
    }
}
