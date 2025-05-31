package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class MegalocerosConfig extends RiftCreatureConfig {
    public MegalocerosConfig() {
        this.stats.baseHealth = 6;
        this.stats.baseDamage = 4;
        this.stats.maxEnergy = 20;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.2;
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
        this.general.blockBreakLevels = Arrays.asList("axe:1", "shovel:0");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(15).setSpawnAmntRange(4, 6).setDensityLimit(10).setBiomes("tag:snowy", "-tag:forest")
        );
    }
}
