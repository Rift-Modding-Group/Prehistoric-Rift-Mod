package anightdazingzoroark.prift.server.config;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Config subclass containing what a creature likes to eat
 * and how said food when eaten affects them
 * */
public class RiftCreatureFood {
    @SerializedName("itemId")
    public String itemId;

    //by how much percent of their maximum health does feeding this to them heal
    @SerializedName("percentHealed")
    public Float percentHealed;

    //by how much of their maximum stamina does feeding this to them regenerate
    @SerializedName("percentReenergized")
    public Float percentReenergized;

    @SerializedName("foodEffects")
    public List<FoodEffect> foodEffects;

    //effects that apply to the creature the moment they eat said food item
    public static class FoodEffect {
        @SerializedName("effectId")
        public String effectId;

        @SerializedName("effectDuration") //is in seconds sadly
        public Integer effectDuration;

        @SerializedName("effectStrength")
        public Integer effectStrength;
    }

    //im lazy
    public static class Builder {
        private final RiftCreatureFood food;

        public Builder(@NotNull String itemId) {
            this.food = new RiftCreatureFood();
            this.food.itemId = itemId;
        }

        public Builder setItemId(@NotNull String itemId) {
            this.food.itemId = itemId;
            return this;
        }

        public Builder setPercentHealed(float percentHealed) {
            this.food.percentHealed = percentHealed;
            return this;
        }

        public Builder setPercentReenergized(float percentReenergized) {
            this.food.percentReenergized = percentReenergized;
            return this;
        }

        public Builder addFoodEffect(@NotNull String effectId, int duration, int strength) {
            RiftCreatureFood.FoodEffect effect = new RiftCreatureFood.FoodEffect();

            effect.effectId = effectId;
            effect.effectDuration = duration;
            effect.effectStrength = strength;

            if (this.food.foodEffects == null) this.food.foodEffects = new ArrayList<>();
            this.food.foodEffects.add(effect);

            return this;
        }

        public RiftCreatureFood build() {
            return this.food;
        }
    }
}
