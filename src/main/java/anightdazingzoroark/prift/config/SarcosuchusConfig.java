package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class SarcosuchusConfig extends RiftCreatureConfig {
    public SarcosuchusConfig() {
        this.stats.baseHealth = 80;
        this.stats.baseDamage = 15;
        this.stats.maxEnergy = 40;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultPiscivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList(
                "minecraft:squid",
                "prift:megapiranha"
        );
        this.general.targetBlacklist = Arrays.asList();
        this.general.maximumSpinAttackTargetSize = "MEDIUM";
        this.general.blockBreakLevels = Arrays.asList("axe:1", "shovel:1");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(56, 64).setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(2).setBiomes("biome:minecraft:river"),
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(56, 64).setWeight(15).setSpawnAmntRange(1, 1).setDensityLimit(2).setBiomes("tag:swamp")
        );
    }
}
