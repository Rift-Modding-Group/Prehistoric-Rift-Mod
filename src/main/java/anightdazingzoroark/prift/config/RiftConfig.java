package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class RiftConfig {
    public Configuration config;
    public String[] spawnPlaces;
    public boolean canSpawn;
    private final String[] initSpawnPlaces;

    public RiftConfig(Configuration config, String[] spawnPlaces) {
        this.config = config;
        this.spawnPlaces = this.initSpawnPlaces = spawnPlaces;
    }

    public void init() {
        this.spawnPlaces = config.getStringList("Areas that this creature will spawn in", "Spawning", this.initSpawnPlaces, "List of biomes this creature will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>:<spawn weight>:<min amount to spawn>:<max amount to spawn>:<insert enum type here>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
        this.canSpawn = config.getBoolean("Can spawn", "Spawning", true, "Manage whether or not this creature can spawn naturally.");
    }
}
