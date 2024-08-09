package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class TyrannosaurusConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public TyrannosaurusGeneral general;

    public TyrannosaurusConfig() {
        this.stats.baseHealth = 160;
        this.stats.baseDamage = 35;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.5;
        this.general = new TyrannosaurusGeneral();
        this.general.saddleItem = "minecraft:saddle";
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultCarnivoreFoods;
        this.general.favoriteMeals = Arrays.asList(
                new Meal("prift:basic_carnivore_meal", 0),
                new Meal("prift:advanced_carnivore_meal", 0)
        );
        this.general.targetWhitelist = Arrays.asList("prift:apatosaurus");
        this.general.targetBlacklist = Arrays.asList();
        this.general.affectedByRoarBlacklist = Arrays.asList(
                "prift:tyrannosaurus",
                "prift:apatosaurus"
        );
        this.general.useRoarBlacklistAsWhitelist = false;
        this.spawnRules.spawnAmntRange = Arrays.asList(1, 1);
        this.spawnRules.densityLimit = 4;
        this.spawnRules.spawnType = "LAND";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:plains", "-tag:savanna"), 7),
                new SpawnBiomes(Arrays.asList("tag:mountain"), 10)
        );
    }

    public static class TyrannosaurusGeneral extends PredatorGeneral {
        @SerializedName("affectedByRoarBlacklist")
        public List<String> affectedByRoarBlacklist;

        @SerializedName("useRoarBlacklistAsWhitelist")
        public boolean useRoarBlacklistAsWhitelist;
    }
}
