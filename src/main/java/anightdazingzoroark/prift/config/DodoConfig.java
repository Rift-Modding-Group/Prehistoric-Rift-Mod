package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class DodoConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public DodoGeneral general = new DodoGeneral();

    public DodoConfig() {
        this.stats.baseHealth = 6;
        this.stats.healthMultiplier = 0.1;
        this.general.breedingFood = Arrays.asList(
                "minecraft:wheat_seeds",
                "minecraft:pumpkin_seeds",
                "minecraft:melon_seeds",
                "minecraft:beetroot_seeds"
        );
        this.spawnRules.spawnAmntRange = Arrays.asList(2, 3);
        this.spawnRules.densityLimit = 16;
        this.spawnRules.spawnType = "LAND";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("tag:plains", "tag:sandy", "tag:forest"), 15)
        );
    }

    public static class DodoGeneral {
        @SerializedName("breedingFood")
        public List<String> breedingFood;
    }
}
