package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class RiftConfig {
    public Configuration config;
    public String[] spawnPlaces;
    private final String[] initSpawnPlaces;

    public RiftConfig(Configuration config, String[] spawnPlaces) {
        this.config = config;
        this.spawnPlaces = this.initSpawnPlaces = spawnPlaces;
    }

    public void init() {
        this.spawnPlaces = config.getStringList("Areas that this creature will spawn in", "Spawning", this.initSpawnPlaces, "List of biomes this creature will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>:<spawn weight>:<min amount to spawn>:<max amount to spawn>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
    }
}
