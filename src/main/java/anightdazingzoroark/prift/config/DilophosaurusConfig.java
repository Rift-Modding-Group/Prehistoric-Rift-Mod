package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class DilophosaurusConfig extends RiftCreatureConfig {
    public DilophosaurusConfig() {
        this.stats.baseHealth = 40;
        this.stats.baseDamage = 2;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0.10),
                new Meal("prift:advanced_carnivore_meal", 0.33)
        );
        this.general.targetWhitelist = Arrays.asList();
        this.general.targetBlacklist = Arrays.asList(
                "prift:stegosaurus",
                "prift:triceratops"
        );
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(16).setBiomes("tag:forest", "tag:jungle", "-tag:cold")
        );
    }
}
