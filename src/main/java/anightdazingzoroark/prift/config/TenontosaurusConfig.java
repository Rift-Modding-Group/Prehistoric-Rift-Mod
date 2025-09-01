package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class TenontosaurusConfig extends RiftCreatureConfig {
    public TenontosaurusConfig() {
        this.stats.baseHealth = 60;
        this.stats.baseDamage = 3;
        this.stats.maxEnergy = 40;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = Arrays.asList(
                new RiftCreatureConfig.Food("minecraft:brown_mushroom", 0.025),
                new RiftCreatureConfig.Food("minecraft:red_mushroom", 0.025)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33)
        );
        this.general.blockBreakLevels = Arrays.asList("shovel:1");
        this.general.breakBlocksInPursuit = true;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("tag:spooky", "tag:forest")
        );
    }
}
