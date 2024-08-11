package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class DirewolfConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public DirewolfGeneral general = new DirewolfGeneral();

    public DirewolfConfig() {
        this.stats.baseHealth = 30;
        this.stats.baseDamage = 6;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList();
        this.general.targetBlacklist = Arrays.asList();
        this.general.sniffableBlocks = Arrays.asList(
                "minecraft:chest:-1",
                "minecraft:trapped_chest:-1",
                "minecraft:ender_chest:-1"
        );
        this.general.blockSniffRange = 16;
        this.general.maximumMobSniffSize = "VERY_LARGE";
        this.general.mobSniffRange = 32;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(12).setSpawnAmntRange(2, 4).setDensityLimit(16).setBiomes("tag:snowy"),
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(12000, 24000).setWeight(24).setSpawnAmntRange(2, 4).setDensityLimit(16).setBiomes("tag:snowy")
        );
    }

    public static class DirewolfGeneral extends PredatorGeneral {
        @SerializedName("sniffableBlocks")
        public List<String> sniffableBlocks;

        @SerializedName("blockSniffRange")
        public int blockSniffRange;

        @SerializedName("maximumMobSniffSize")
        public String maximumMobSniffSize;

        @SerializedName("mobSniffRange")
        public int mobSniffRange;
    }
}
