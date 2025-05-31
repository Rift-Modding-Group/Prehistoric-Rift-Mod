package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class DodoConfig extends RiftCreatureConfig {
    public DodoConfig() {
        this.stats.baseHealth = 6;
        this.stats.healthMultiplier = 0.1;
        this.stats.maxEnergy = 20;
        this.general.favoriteFood = Arrays.asList(
                new Food("minecraft:wheat_seeds", 0.05),
                new Food("minecraft:pumpkin_seeds", 0.05),
                new Food("minecraft:melon_seeds", 0.05),
                new Food("minecraft:beetroot_seeds", 0.05)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("minecraft:wheat_seeds", 0),
                new Meal("minecraft:pumpkin_seeds", 0),
                new Meal("minecraft:melon_seeds", 0),
                new Meal("minecraft:beetroot_seeds", 0)
        );
        this.general.blockBreakLevels = Arrays.asList();
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(10).setSpawnAmntRange(2, 3).setDensityLimit(16).setBiomes("tag:plains", "tag:sandy", "tag:forest")
        );
    }
}
