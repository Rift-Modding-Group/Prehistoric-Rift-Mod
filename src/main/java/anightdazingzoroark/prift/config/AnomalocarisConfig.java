package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class AnomalocarisConfig extends RiftCreatureConfig {
    public AnomalocarisConfig() {
        this.stats.baseHealth = 50;
        this.stats.baseDamage = 5;
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
                "prift:coelacanth",
                "prift:megapiranha"
        );
        this.general.targetBlacklist = Arrays.asList();
        this.general.maximumGrabTargetSize = "MEDIUM";
        this.general.blockBreakLevels = Arrays.asList("pickaxe:0", "shovel:0");
        this.general.breakBlocksInPursuit = true;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(0, 64).setWeight(5).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("biome:minecraft:deep_ocean")
        );
    }
}
