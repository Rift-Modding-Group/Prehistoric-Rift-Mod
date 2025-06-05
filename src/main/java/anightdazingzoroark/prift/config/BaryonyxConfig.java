package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class BaryonyxConfig extends RiftCreatureConfig {
    public BaryonyxConfig() {
        this.stats.baseHealth = 60;
        this.stats.baseDamage = 10;
        this.stats.maxEnergy = 80;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList("minecraft:squid");
        this.general.targetBlacklist = Arrays.asList();
        this.general.blockBreakLevels = Arrays.asList("pickaxe:0", "axe:0", "shovel:0");
        this.general.breakBlocksInPursuit = true;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setSpawnInWater().setMustSeeSky().setYLevelRange(56, 256).setTimeRange(0, 12000).setWeight(15).setSpawnAmntRange(1, 1).setDensityLimit(2).setBiomes("tag:swamp")
        );
    }
}
