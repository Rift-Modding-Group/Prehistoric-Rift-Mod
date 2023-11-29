package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class RiftConfig {
    public Configuration config;
    private static int maxHealth;
    public static int damage;
    private int initMaxHealth;
    private int initDamage;
    public String[] spawnPlaces;
    private final String[] initSpawnPlaces;

    public RiftConfig(Configuration config, String[] spawnPlaces, int maxHealth, int damage) {
        this.config = config;
        this.spawnPlaces = this.initSpawnPlaces = spawnPlaces;
        this.maxHealth = this.initMaxHealth = maxHealth;
        this.damage = this.initDamage = damage;
    }

    public void init() {
        this.maxHealth = config.getInt("Max health for this creature", "Creature Stats", this.initMaxHealth, 1, 69420, "Maximum health of this creature");
        this.damage = config.getInt("Max damage for this creature", "Creature Stats", this.initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        this.spawnPlaces = config.getStringList("Areas that this creature will spawn in", "Spawning", this.initSpawnPlaces, "List of biomes this creature will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>:<spawn weight>:<min amount to spawn>:<max amount to spawn>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
