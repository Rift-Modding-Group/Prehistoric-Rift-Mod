package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class AnkylosaurusConfig extends RiftCreatureConfig {
    public AnkylosaurusConfig() {
        this.stats.baseHealth = 80;
        this.stats.baseDamage = 25;
        this.stats.maxEnergy = 120;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = Arrays.asList(
                new RiftCreatureConfig.Food("minecraft:apple", 0.025),
                new RiftCreatureConfig.Food("minecraft:wheat", 0.025),
                new RiftCreatureConfig.Food("minecraft:carrot", 0.025),
                new RiftCreatureConfig.Food("minecraft:potato", 0.025),
                new RiftCreatureConfig.Food("minecraft:beetroot", 0.025),
                new RiftCreatureConfig.Food("minecraft:cactus", 0.025)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33)
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
        this.general.blockBreakLevels = Arrays.asList("pickaxe:2", "axe:0", "shovel:0");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(15).setSpawnAmntRange(4, 6).setDensityLimit(12).setBiomes("tag:sandy")
        );

    }
}
