package anightdazingzoroark.prift.configNew;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public abstract class RiftCreatureConfig {
    @SerializedName("stats")
    public Stats stats = new Stats();

    @SerializedName("spawnRules")
    public List<SpawnRule> spawnRules;

    public static class Stats {
        @SerializedName("baseHealth")
        public int baseHealth;

        @SerializedName("baseDamage")
        public int baseDamage;

        @SerializedName("healthMultiplier")
        public double healthMultiplier;

        @SerializedName("damageMultiplier")
        public double damageMultiplier;
    }

    public static class General {
        @SerializedName("saddleItem")
        public String saddleItem;

        @SerializedName("favoriteFood")
        public List<Food> favoriteFood;

        @SerializedName("favoriteMeals")
        public List<Meal> favoriteMeals;
    }

    public static class PredatorGeneral extends General {
        @SerializedName("targetWhitelist")
        public List<String> targetWhitelist;

        @SerializedName("targetBlacklist")
        public List<String> targetBlacklist;
    }

    public static class Food {
        @SerializedName("itemId")
        public String itemId;

        @SerializedName("percentageHeal")
        public double percentageHeal;

        //@SerializedName("effectsToApply")

        public Food(String itemId, double percentageHeal) {
            this.itemId = itemId;
            this.percentageHeal = percentageHeal;
        }
    }

    public static class Meal {
        @SerializedName("itemId")
        public String itemId;

        @SerializedName("tameMultiplier")
        public double tameMultiplier;

        public Meal(String itemId, double tameMultiplier) {
            this.itemId = itemId;
            this.tameMultiplier = tameMultiplier;
        }
    }

    public static class SpawnRule {
        //this is the spawning category that determines the bulk of where a creature should spawn in
        //values are:
        //LAND: land creatures at day
        //NOCTURNAL_LAND: land creatures at night
        //WATER: aquatic creatures
        //AMPHIBIOUS: for amphibious creatures, will count as both land and water
        //AIR: for flying creatures
        //CAVE:  cave creatures
        @SerializedName("category")
        public String category;

        //among all the creatures within the biome the player is in and the spawning category,
        //the weight will be used to determine what creatures will spawn
        //higher values means higher chance
        @SerializedName("weight")
        public int weight;

        //an array w a size of 2 and only 2
        //is for how many creatures will be placed
        //during the spawning phase
        //0 is min, 1 is max
        @SerializedName("spawnAmntRange")
        public List<Integer> spawnAmntRange;

        //how many creatures of this type within a 64 x 64 x 64
        //should exist
        @SerializedName("densityLimit")
        public int densityLimit;

        //blocks that a creature will usually spawn on
        @SerializedName("spawnBlocks")
        public List<String> spawnBlocks;

        //whether or not a creature should spawn on rain
        @SerializedName("inRain")
        public boolean inRain;

        //obviously the biomes a creature should be in
        @SerializedName("biomes")
        public List<String> biomes;

        //if gamestages is installed, make it so that this creature
        //will only spawn in certain gamestages
        @SerializedName("gamestage")
        public String gamestage;

        //if serene seasons is installed, make it so that this creature
        //will only spawn during this season
        @SerializedName("season")
        public String season;

        public SpawnRule setCategory(String value) {
            this.category = value;
            return this;
        }

        public SpawnRule setWeight(int value) {
             this.weight = value;
             return this;
        }

        public SpawnRule setSpawnAmntRange(int min, int max) {
            this.spawnAmntRange = Arrays.asList(min, max);
            return this;
        }

        public SpawnRule setDensityLimit(int value) {
            this.densityLimit = value;
            return this;
        }

        public SpawnRule setSpawnBlocks(List<String> values) {
            this.spawnBlocks = values;
            return this;
        }

        public SpawnRule setSpawnInRain() {
            this.inRain = true;
            return this;
        }

        public SpawnRule setBiomes(List<String> values) {
            this.biomes = values;
            return this;
        }
    }
}
