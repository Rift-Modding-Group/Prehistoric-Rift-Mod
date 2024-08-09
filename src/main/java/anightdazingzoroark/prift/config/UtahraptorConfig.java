package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class UtahraptorConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public PredatorGeneral general = new PredatorGeneral();

    public UtahraptorConfig() {
        this.stats.baseHealth = 30;
        this.stats.baseDamage = 4;
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
        this.spawnRules.spawnAmntRange = Arrays.asList(2, 4);
        this.spawnRules.densityLimit = 16;
        this.spawnRules.spawnType = "LAND";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:plains", "-tag:savanna"), 7),
                new SpawnBiomes(Arrays.asList("tag:plains", "-tag:savanna"), 120).setSpawnAtNightOnly(),
                new SpawnBiomes(Arrays.asList("tag:forest", "tag:jungle", "-tag:cold"), 15),
                new SpawnBiomes(Arrays.asList("tag:forest", "tag:jungle", "-tag:cold"), 120).setSpawnAtNightOnly()
        );
    }
}
