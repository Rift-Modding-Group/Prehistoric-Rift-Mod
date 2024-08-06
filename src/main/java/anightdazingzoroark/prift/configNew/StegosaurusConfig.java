package anightdazingzoroark.prift.configNew;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class StegosaurusConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public StegosaurusGeneral general = new StegosaurusGeneral();

    public StegosaurusConfig() {
        this.stats.baseHealth = 100;
        this.stats.baseDamage = 30;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultHerbivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_herbivore_meal", 0.1),
                new Meal("prift:advanced_herbivore_meal", 0.33)
        );
        this.general.canInflictBleed = false;
        this.general.harvestableBlocks = Arrays.asList(
                "minecraft:wheat:7",
                "minecraft:carrots:7",
                "minecraft:potatoes:7",
                "minecraft:beetroots:3",
                "prift:pyroberry_bush:2",
                "prift:pyroberry_bush:3",
                "prift:cryoberry_bush:2",
                "prift:cryoberry_bush:3"
        );
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setWeight(10).setSpawnAmntRange(4, 6).setDensityLimit(12).setBiomes(Arrays.asList("tag:plains", "-tag:savanna"))
        );
    }

    public static class StegosaurusGeneral extends General {
        @SerializedName("canInflictBleed")
        public boolean canInflictBleed;

        @SerializedName("harvestableBlocks")
        public List<String> harvestableBlocks;
    }
}