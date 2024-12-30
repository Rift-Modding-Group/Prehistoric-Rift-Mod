package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.RiftInitialize;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.common.Optional;

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
        //spawn categories
        //these only affect the spawn group its in
        //valid values are:
        //LAND, WATER, CAVE, and AIR
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

        //additional blocks that a creature will usually spawn on
        @SerializedName("spawnBlocksWhitelist")
        public List<String> spawnBlocksWhitelist;

        //additional blocks that a creature will usually spawn on
        @SerializedName("spawnBlocksBlacklist")
        public List<String> spawnBlocksBlacklist;

        //whether or not a creature should spawn
        //during rain
        @SerializedName("inRain")
        public boolean inRain;

        //whether or not a creature should spawn on land
        @SerializedName("onLand")
        public boolean onLand;

        //whether or not a creature should spawn in water
        @SerializedName("inWater")
        public boolean inWater;

        //whether or not a creature should spawn in air
        @SerializedName("inAir")
        public boolean inAir;

        //whether or not a creature should
        //spawn underground
        @SerializedName("mustSeeSky")
        public boolean mustSeeSky;

        //time range in which a mob can spawn in
        @SerializedName("timeRange")
        public List<Integer> timeRange = Arrays.asList(0, 24000);

        //y level range
        @SerializedName("yLevelRange")
        public List<Integer> yLevelRange = Arrays.asList(0, 256);

        //dimension id that the creature can only spawn in
        //by default its the overworld only
        @SerializedName("dimensionId")
        public int dimensionId = 0;

        //obviously the biomes a creature should be in
        @SerializedName("biomes")
        public List<String> biomes;

        //if gamestages is installed, make it so that this creature
        //will only spawn in certain gamestages
        @SerializedName("gamestage")
        public String gamestage;

        //if serene seasons is installed, make it so that this creature
        //will only spawn during certain seasons
        @SerializedName("seasons")
        public List<String> seasons;

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

        public SpawnRule setSpawnBlocksWhitelist(String... values) {
            this.spawnBlocksWhitelist = Arrays.asList(values);
            return this;
        }

        public SpawnRule setSpawnBlocksBlacklist(String... values) {
            this.spawnBlocksBlacklist = Arrays.asList(values);
            return this;
        }

        public SpawnRule setSpawnInRain() {
            this.inRain = true;
            return this;
        }

        public SpawnRule setSpawnOnLand() {
            this.onLand = true;
            return this;
        }

        public SpawnRule setSpawnInWater() {
            this.inWater = true;
            return this;
        }

        public SpawnRule setSpawnInAir() {
            this.inAir = true;
            return this;
        }

        public SpawnRule setMustSeeSky() {
            this.mustSeeSky = true;
            return this;
        }

        public SpawnRule setTimeRange(int min, int max) {
            if (max >= min) this.timeRange = Arrays.asList(Math.max(0, min), Math.min(24000, max));
            return this;
        }

        public SpawnRule setYLevelRange(int min, int max) {
            if (max >= min) this.yLevelRange = Arrays.asList(Math.max(0, min), Math.min(256, max));
            return this;
        }

        public SpawnRule setDimensionId(int value) {
            this.dimensionId = value;
            return this;
        }

        public SpawnRule setBiomes(String... values) {
            this.biomes = Arrays.asList(values);
            return this;
        }

        @Optional.Method(modid = RiftInitialize.SERENE_SEASONS_MOD_ID)
        public SpawnRule setSeasons(String... values) {
            this.biomes = Arrays.asList(values);
            return this;
        }
    }
}
