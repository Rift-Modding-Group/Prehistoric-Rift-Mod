package anightdazingzoroark.prift.configNew;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class UtahraptorConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public PredatorGeneral general = new PredatorGeneral();

    public UtahraptorConfig() {
        this.stats.baseHealth = 30;
        this.stats.baseDamage = 4;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0),
                new Meal("prift:advanced_carnivore_meal", 0)
        );
        this.general.targetWhitelist = Arrays.asList();
        this.general.targetBlacklist = Arrays.asList();
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setWeight(10).setSpawnAmntRange(2, 4).setDensityLimit(16).setBiomes(Arrays.asList("tag:plains", "-tag:savanna")),
                new SpawnRule().setCategory("LAND").setWeight(15).setSpawnAmntRange(2, 4).setDensityLimit(16).setBiomes(Arrays.asList("tag:forest", "tag:jungle", "-tag:cold"))
        );
    }
}
