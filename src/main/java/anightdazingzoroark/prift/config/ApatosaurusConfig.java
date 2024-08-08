package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class ApatosaurusConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public ApatosaurusGeneral general = new ApatosaurusGeneral();

    public ApatosaurusConfig() {
        this.stats.baseHealth = 200;
        this.stats.baseDamage = 80;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "prift:apatosaurus_platform";
        this.general.favoriteFood = Arrays.asList(
                new RiftCreatureConfig.Food("minecraft:leaves:-1", 0.025),
                new RiftCreatureConfig.Food("minecraft:leaves2:-1", 0.025)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0),
                new Meal("prift:advanced_herbivore_meal", 0)
        );
        this.general.maximumPassengerSize = "MEDIUM";
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setWeight(7).setSpawnAmntRange(1, 3).setDensityLimit(4).setBiomes(Arrays.asList("tag:plains", "-tag:savanna"))
        );
    }

    public static class ApatosaurusGeneral extends General {
        @SerializedName("maximumPassengerSize")
        public String maximumPassengerSize;
    }
}
