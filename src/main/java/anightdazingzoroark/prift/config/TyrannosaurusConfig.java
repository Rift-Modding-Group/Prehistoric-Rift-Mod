package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class TyrannosaurusConfig extends RiftCreatureConfig {
    public TyrannosaurusConfig() {
        this.stats.baseHealth = 160;
        this.stats.baseDamage = 35;
        this.stats.maxEnergy = 160;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0),
                new Meal("prift:advanced_carnivore_meal", 0)
        );
        this.general.targetWhitelist = Arrays.asList("prift:apatosaurus", "prift:ankylosaurus");
        this.general.targetBlacklist = Arrays.asList();
        this.general.affectedByRoarBlacklist = Arrays.asList(
                "prift:tyrannosaurus",
                "prift:apatosaurus"
        );
        this.general.useRoarBlacklistAsWhitelist = false;
        this.general.blockBreakLevels = Arrays.asList("pickaxe:2", "axe:2", "shovel:2");
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(7).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("tag:plains", "-tag:savanna", "-tag:cold"),
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(10).setSpawnAmntRange(1, 1).setDensityLimit(4).setBiomes("tag:mountain", "-tag:cold")
        );
    }
}
