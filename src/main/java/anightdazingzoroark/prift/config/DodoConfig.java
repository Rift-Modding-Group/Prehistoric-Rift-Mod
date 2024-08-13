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
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("LAND").setSpawnOnLand().setMustSeeSky().setTimeRange(0, 12000).setWeight(10).setSpawnAmntRange(2, 3).setDensityLimit(16).setBiomes("tag:plains", "tag:sandy", "tag:forest")
        );
    }

    public static class DodoGeneral {
        @SerializedName("breedingFood")
        public List<String> breedingFood;
    }
}
