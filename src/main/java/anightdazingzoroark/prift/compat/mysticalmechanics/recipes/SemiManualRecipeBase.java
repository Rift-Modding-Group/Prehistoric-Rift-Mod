package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import net.minecraft.util.ResourceLocation;

public abstract class SemiManualRecipeBase {
    private ResourceLocation id;
    private double minPower;
    private double time; //time is in ticks

    public SemiManualRecipeBase(ResourceLocation id, double minPower, double time) {
        this.id = id;
        this.minPower = minPower;
        this.time = time;
    }

    public String getId() {
        return id.toString();
    }

    public double getMinPower() {
        return this.minPower;
    }

    public double getTime() {
        return this.time;
    }
}
