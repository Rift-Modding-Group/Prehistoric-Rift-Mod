package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class PalaeocastorConfig extends RiftCreatureConfig {
    public PalaeocastorConfig() {
        this.stats.baseHealth = 20;
        this.stats.baseDamage = 5;
        this.stats.maxEnergy = 20;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.favoriteFood = Arrays.asList(
                new RiftCreatureConfig.Food("minecraft:coal:0", 0.05),
                new RiftCreatureConfig.Food("minecraft:cobblestone", 0.025),
                new RiftCreatureConfig.Food("minecraft:stone:0", 0.05),
                new RiftCreatureConfig.Food("minecraft:stone:1", 0.05),
                new RiftCreatureConfig.Food("minecraft:stone:2", 0.2),
                new RiftCreatureConfig.Food("minecraft:stone:3", 0.05),
                new RiftCreatureConfig.Food("minecraft:stone:4", 0.2),
                new RiftCreatureConfig.Food("minecraft:stone:5", 0.05),
                new RiftCreatureConfig.Food("minecraft:stone:6", 0.2)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_saxumavore_meal", 0.10),
                new Meal("prift:advanced_saxumavore_meal", 0.33)
        );
        this.general.harvestableBlocks = Arrays.asList(
                "minecraft:coal_ore:0",
                "minecraft:iron_ore:0",
                "minecraft:lapis_ore:0",
                "minecraft:gold_ore:0",
                "minecraft:diamond_ore:0",
                "minecraft:emerald_ore:0",
                "minecraft:cobblestone:0"
        );
        this.general.blockBreakLevels = Arrays.asList("pickaxe:2", "shovel:0");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("CAVE").setSpawnOnLand().setYLevelRange(0, 56).setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("all")
        );
    }
}
