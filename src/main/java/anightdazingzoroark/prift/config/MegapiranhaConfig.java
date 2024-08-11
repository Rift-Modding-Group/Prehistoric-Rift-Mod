package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class MegapiranhaConfig extends RiftCreatureConfig {
    @SerializedName("general")
    public PredatorGeneral general = new PredatorGeneral();

    public MegapiranhaConfig() {
        this.stats.baseHealth = 4;
        this.stats.baseDamage = 2;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.2;
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultPiscivoreFoods;
        this.general.targetWhitelist = Arrays.asList(
                "prift:tyrannosaurus",
                "prift:utahraptor",
                "prift:apatosaurus",
                "prift:dimetrodon",
                "prift:saurophaganax",
                "prift:direwolf",
                "minecraft:squid"
        );
        this.general.targetBlacklist = Arrays.asList();
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setYLevelRange(0, 64).setWeight(25).setSpawnAmntRange(4, 6).setDensityLimit(16).setBiomes("all", "-tag:ocean", "-tag:beach")
        );
    }
}
