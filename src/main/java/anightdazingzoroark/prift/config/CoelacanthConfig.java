package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class CoelacanthConfig extends RiftCreatureConfig {
    public CoelacanthConfig() {
        this.stats.baseHealth = 6;
        this.stats.healthMultiplier = 0.1;
        this.spawnRules.spawnAmntRange = Arrays.asList(4, 6);
        this.spawnRules.densityLimit = 16;
        this.spawnRules.spawnType = "WATER";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("biome:minecraft:deep_ocean"), 15)
        );
    }
}
