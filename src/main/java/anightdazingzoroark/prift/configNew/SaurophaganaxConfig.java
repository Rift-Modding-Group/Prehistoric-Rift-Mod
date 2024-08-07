package anightdazingzoroark.prift.configNew;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class SaurophaganaxConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public PredatorGeneral general = new PredatorGeneral();

    public SaurophaganaxConfig() {
        this.stats.baseHealth = 100;
        this.stats.baseDamage = 60;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = Arrays.asList(
                new RiftCreatureConfig.Food("minecraft:rotten_flesh", 0.05),
                new RiftCreatureConfig.Food("minecraft:bone", 0.05),
                new RiftCreatureConfig.Food("minecraft:gunpowder", 0.05),
                new RiftCreatureConfig.Food("minecraft:spider_eye", 0.05),
                new RiftCreatureConfig.Food("minecraft:fermented_spider_eye", 0.025),
                new RiftCreatureConfig.Food("minecraft:slime_ball", 0.05),
                new RiftCreatureConfig.Food("minecraft:ender_pearl", 0.05),
                new RiftCreatureConfig.Food("minecraft:ghast_tear", 0.05),
                new RiftCreatureConfig.Food("minecraft:blaze_rod", 0.05),
                new RiftCreatureConfig.Food("minecraft:blaze_powder", 0.025),
                new RiftCreatureConfig.Food("minecraft:magma_cream", 0.05),
                new RiftCreatureConfig.Food("prift:raw_hemolymph", 0.025),
                new RiftCreatureConfig.Food("prift:cooked_hemolymph", 0.05)
        );
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList(
                "minecraft:cave_spider",
                "minecraft:enderman",
                "minecraft:spider",
                "minecraft:zombie_pigman",
                "minecraft:blaze",
                "minecraft:creeper",
                "minecraft:elder_guardian",
                "minecraft:endermite",
                "minecraft:ghast",
                "minecraft:guardian",
                "minecraft:husk",
                "minecraft:magma_cube",
                "minecraft:shulker",
                "minecraft:silverfish",
                "minecraft:skeleton",
                "minecraft:slime",
                "minecraft:stray",
                "minecraft:wither_skeleton",
                "minecraft:zombie",
                "minecraft:zombie_villager"
        );
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("NOCTURNAL_LAND").setWeight(15).setSpawnAmntRange(1, 1).setDensityLimit(1).setBiomes(Arrays.asList("all")),
                new SpawnRule().setCategory("CAVE").setWeight(15).setSpawnAmntRange(1, 1).setDensityLimit(1).setBiomes(Arrays.asList("all"))
        );
    }
}
