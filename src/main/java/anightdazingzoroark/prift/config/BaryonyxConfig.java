package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class BaryonyxConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public PredatorGeneral general = new PredatorGeneral();

    public BaryonyxConfig() {
        this.stats.baseHealth = 60;
        this.stats.baseDamage = 10;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList();
        this.general.targetBlacklist = Arrays.asList();
        this.spawnRules.spawnAmntRange = Arrays.asList(1, 1);
        this.spawnRules.densityLimit = 2;
        this.spawnRules.spawnType = "WATER";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:swamp"), 10)
        );
    }
}
