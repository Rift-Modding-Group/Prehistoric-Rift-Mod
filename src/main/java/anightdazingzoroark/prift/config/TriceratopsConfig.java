package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class TriceratopsConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public TriceratopsGeneral general = new TriceratopsGeneral();

    public TriceratopsConfig() {
        this.stats.baseHealth = 80;
        this.stats.baseDamage = 25;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultHerbivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33)
        );
        this.general.harvestableBlocks = Arrays.asList(
                "minecraft:log:-1",
                "minecraft:log2:-1",
                "minecraft:leaves:-1",
                "minecraft:leaves2:-1"
        );
        this.spawnRules.spawnAmntRange = Arrays.asList(4, 6);
        this.spawnRules.densityLimit = 10;
        this.spawnRules.spawnType = "LAND";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:plains", "-tag:savanna"), 10)
        );
    }

    public static class TriceratopsGeneral extends General {
        @SerializedName("harvestableBlocks")
        public List<String> harvestableBlocks;
    }
}
