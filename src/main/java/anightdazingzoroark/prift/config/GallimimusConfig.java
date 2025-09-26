package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class GallimimusConfig extends RiftCreatureConfig {
    public GallimimusConfig() {
        this.stats.baseHealth = 20;
        this.stats.baseDamage = 0;
        this.stats.maxEnergy = 20;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultHerbivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33),
                new Meal("prift:basic_carnivore_meal", 0.1),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.mobsToRunFrom = Arrays.asList(
                "family:human",
                "family:carnivore"
        );
        this.general.blockBreakLevels = Arrays.asList("shovel:0");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(10).setSpawnAmntRange(4, 6).setDensityLimit(12).setBiomes("tag:savanna")
        );
    }
}
