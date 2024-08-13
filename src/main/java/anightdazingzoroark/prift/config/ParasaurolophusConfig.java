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
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(20).setSpawnAmntRange(4, 6).setDensityLimit(12).setBiomes("tag:plains", "-tag:savanna")
        );
    }

    public static class ParasaurolophusGeneral extends General {
        @SerializedName("harvestableBlocks")
        public List<String> harvestableBlocks;
    }
}
