package anightdazingzoroark.prift.config;

import java.util.Arrays;

public class CoelacanthConfig extends RiftCreatureConfig {
    public CoelacanthConfig() {
        this.stats.baseHealth = 6;
        this.stats.healthMultiplier = 0.1;
        this.stats.maxEnergy = 100;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(0, 64).setWeight(15).setSpawnAmntRange(4, 6).setDensityLimit(16).setBiomes("biome:minecraft:deep_ocean")
        );
    }
}
