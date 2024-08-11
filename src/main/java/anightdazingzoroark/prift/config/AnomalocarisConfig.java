package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class AnomalocarisConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public AnomalocarisGeneral general = new AnomalocarisGeneral();

    public AnomalocarisConfig() {
        this.stats.baseHealth = 50;
        this.stats.baseDamage = 5;
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
        this.general.maximumGrabTargetSize = "MEDIUM";
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(0, 64).setWeight(5).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("biome:minecraft:deep_ocean")
        );
    }

    public static class AnomalocarisGeneral extends PredatorGeneral {
        @SerializedName("maximumGrabTargetSize")
        public String maximumGrabTargetSize;
    }
}
