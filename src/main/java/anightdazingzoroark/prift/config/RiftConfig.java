package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class RiftConfig {
    public Configuration config;
    public String[] spawnPlaces;
    public boolean canSpawn;
    private final String[] initSpawnPlaces;
    protected static final String healthConfigName = "Base health for this creature";
    protected static final String healthConfigMessage = "Health of this creature at level 1";
    protected static final String damageConfigName = "Base damage for this creature";
    protected static final String damageConfigMessage = "(Melee) Damage of this creature at level 1";
    protected static final String healthMultiplierConfigName = "Health multiplier (due to level) of this creature";
    protected static final String healthMultiplierConfigMessage = "Health multiplier of this creature. Note that the formula for creature health is equal to baseHealth + (healthMultiplier * baseHealth * (level - 1))";
    protected static final String damageMultiplierConfigName = "Damage multiplier (due to level) of this creature";
    protected static final String damageMultiplierConfigMessage = "Damage multiplier of this creature. Note that the formula for creature damage is equal to baseDamage + (damageMultiplier * (level - 1))";

    public RiftConfig(Configuration config, String[] spawnPlaces) {
        this.config = config;
        this.spawnPlaces = this.initSpawnPlaces = spawnPlaces;
    }

    public void init() {
        this.spawnPlaces = config.getStringList("Areas that this creature will spawn in", "Spawning", this.initSpawnPlaces, "List of biomes this creature will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>:<spawn weight>:<min amount to spawn>:<max amount to spawn>:<insert enum type here>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
        this.canSpawn = config.getBoolean("Can spawn", "Spawning", true, "Manage whether or not this creature can spawn naturally.");
    }
}
