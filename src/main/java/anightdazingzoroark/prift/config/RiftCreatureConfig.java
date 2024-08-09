package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public abstract class RiftCreatureConfig {
    @SerializedName("stats")
    public Stats stats = new Stats();

    @SerializedName("spawnRules")
    public SpawnRules spawnRules = new SpawnRules();

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

    public static class SpawnRules {
        //an array w a size of 2 and only 2
        //is for how many creatures will be placed
        //during the spawning phase
        //0 is min, 1 is max
        @SerializedName("spawnAmntRange")
        public List<Integer> spawnAmntRange;

        //how many creatures of this type within a 64 x 64 x 64 range
        //should exist
        @SerializedName("densityLimit")
        public int densityLimit;

        //blocks that a creature will usually spawn on
        @SerializedName("spawnBlocks")
        public List<String> spawnBlocks;

        @SerializedName("spawnType")
        public String spawnType;

        //spawn biomes
        @SerializedName("spawnBiomes")
        public List<SpawnBiomes> spawnBiomes;

        /*
        //if gamestages is installed, make it so that this creature
        //will only spawn in certain gamestages
        @SerializedName("gamestage")
        public String gamestage;

        //if serene seasons is installed, make it so that this creature
        //will only spawn during this season
        @SerializedName("season")
        public String season;
        */
    }

    public static class SpawnBiomes {
        //obviously the biomes a creature should be in
        @SerializedName("biomes")
        public List<String> biomes;

        //weight for mob spawning
        @SerializedName("weight")
        public int weight;

        //whether or not it will only spawn at night
        @SerializedName("spawnAtNightOnly")
        public boolean spawnAtNightOnly;

        public SpawnBiomes(List<String> biomes, int weight) {
            this.biomes = biomes;
            this.weight = weight;
        }

        public SpawnBiomes setSpawnAtNightOnly() {
            this.spawnAtNightOnly = true;
            return this;
        }
    }
}
