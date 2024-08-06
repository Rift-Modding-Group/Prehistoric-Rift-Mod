package anightdazingzoroark.prift.configNew;

import java.util.Arrays;

public class CoelacanthConfig extends RiftCreatureConfig {
    public CoelacanthConfig() {
        this.stats.baseHealth = 6;
        this.stats.healthMultiplier = 0.1;
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setWeight(15).setSpawnAmntRange(4, 6).setDensityLimit(16).setBiomes(Arrays.asList("biome:minecraft:deep_ocean"))
        );
    }
}
