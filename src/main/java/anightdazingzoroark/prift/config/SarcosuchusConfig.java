package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class SarcosuchusConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public SarcosuchusGeneral general = new SarcosuchusGeneral();

    public SarcosuchusConfig() {
        this.stats.baseHealth = 80;
        this.stats.baseDamage = 15;
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
        this.general.maximumSpinAttackTargetSize = "MEDIUM";
        this.spawnRules.spawnAmntRange = Arrays.asList(1, 1);
        this.spawnRules.densityLimit = 2;
        this.spawnRules.spawnType = "WATER";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("biome:minecraft:river"), 10),
                new SpawnBiomes(Arrays.asList("tag:swamp"), 15)
        );
    }

    public static class SarcosuchusGeneral extends PredatorGeneral {
        @SerializedName("maximumSpinAttackTargetSize")
        public String maximumSpinAttackTargetSize;
    }
}
