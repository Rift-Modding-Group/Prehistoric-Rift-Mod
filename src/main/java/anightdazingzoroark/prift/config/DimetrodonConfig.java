package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class DimetrodonConfig extends RiftCreatureConfig {
    @SerializedName("simpleDifficulty")
    public SimpleDifficulty simpleDifficulty = new SimpleDifficulty();

    public DimetrodonConfig() {
        this.stats.baseHealth = 40;
        this.stats.baseDamage = 8;
        this.stats.maxEnergy = 40;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList();
        this.general.targetBlacklist = Arrays.asList(
                "prift:stegosaurus",
                "prift:triceratops",
                "prift:parasaurolophus",
                "minecraft:player"
        );
        this.general.temperatureChangingItems = Arrays.asList(
                new TemperatureChangingItem("prift:extreme_frost_stimulant", "VERY_COLD", 9600),
                new TemperatureChangingItem("prift:frost_stimulant", "COLD", 9600),
                new TemperatureChangingItem("prift:neutral_stimulant", "NEUTRAL", 9600),
                new TemperatureChangingItem("prift:flame_stimulant", "WARM", 9600),
                new TemperatureChangingItem("prift:extreme_flame_stimulant", "VERY_WARM", 9600)
        );
        this.simpleDifficulty.veryColdTemperatureValue = -10.0f;
        this.simpleDifficulty.coldTemperatureValue = -5.0f;
        this.simpleDifficulty.warmTemperatureValue = 5.0f;
        this.simpleDifficulty.veryWarmTemperatureValue = 10.0f;
        this.general.blockBreakLevels = Arrays.asList("pickaxe:0", "axe:0", "shovel:0");
        this.general.breakBlocksInPursuit = true;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("tag:sandy", "tag:savanna")
        );
    }

    public static class SimpleDifficulty {
        @SerializedName("veryColdTemperatureValue")
        public float veryColdTemperatureValue;

        @SerializedName("coldTemperatureValue")
        public float coldTemperatureValue;

        @SerializedName("warmTemperatureValue")
        public float warmTemperatureValue;

        @SerializedName("veryWarmTemperatureValue")
        public float veryWarmTemperatureValue;
    }

    public static class TemperatureChangingItem {
        @SerializedName("itemId")
        public String itemId;

        @SerializedName("temperatureMode")
        public String temperatureMode;

        @SerializedName("ticks")
        public int ticks;

        public TemperatureChangingItem(String itemId, String temperatureMode, int ticks) {
            this.itemId = itemId;
            this.temperatureMode = temperatureMode;
            this.ticks = ticks;
        }
    }
}
