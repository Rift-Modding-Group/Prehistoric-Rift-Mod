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
        this.spawnRules.spawnAmntRange = Arrays.asList(1, 1);
        this.spawnRules.densityLimit = 4;
        this.spawnRules.spawnType = "WATER";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("biome:minecraft:deep_ocean"), 5)
        );
    }

    public static class AnomalocarisGeneral extends PredatorGeneral {
        @SerializedName("maximumGrabTargetSize")
        public String maximumGrabTargetSize;
    }
}
