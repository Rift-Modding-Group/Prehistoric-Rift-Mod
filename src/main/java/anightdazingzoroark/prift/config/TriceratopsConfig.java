package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class TriceratopsConfig extends RiftCreatureConfig {
    public TriceratopsConfig() {
        this.stats.baseHealth = 80;
        this.stats.baseDamage = 25;
        this.stats.maxEnergy = 120;
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
        this.general.blockBreakLevels = Arrays.asList("pickaxe:1", "axe:1", "shovel:1");
        this.general.breakBlocksInPursuit = true;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(15).setSpawnAmntRange(4, 6).setDensityLimit(10).setBiomes("tag:plains", "-tag:savanna")
        );
    }
}
