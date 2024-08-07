package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class ParasaurolophusConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public ParasaurolophusGeneral general = new ParasaurolophusGeneral();

    public ParasaurolophusConfig() {
        this.stats.baseHealth = 60;
        this.stats.baseDamage = 0;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultHerbivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33)
        );
        this.general.harvestableBlocks = Arrays.asList(
                "minecraft:wheat:7",
                "minecraft:carrots:7",
                "minecraft:potatoes:7",
                "minecraft:beetroots:3",
                "prift:pyroberry_bush:2",
                "prift:pyroberry_bush:3",
                "prift:cryoberry_bush:2",
                "prift:cryoberry_bush:3"
        );
        this.spawnRules.spawnAmntRange = Arrays.asList(4, 6);
        this.spawnRules.densityLimit = 12;
        this.spawnRules.spawnType = "LAND";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:plains", "-tag:savanna"), 10)
        );
    }

    public static class ParasaurolophusGeneral extends General {
        @SerializedName("harvestableBlocks")
        public List<String> harvestableBlocks;
    }
}
