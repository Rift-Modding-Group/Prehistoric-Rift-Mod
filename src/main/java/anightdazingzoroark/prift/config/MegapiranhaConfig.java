package anightdazingzoroark.prift.config;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class MegapiranhaConfig extends RiftCreatureConfig {
    public MegapiranhaConfig() {
        this.stats.baseHealth = 4;
        this.stats.baseDamage = 2;
        this.stats.healthMultiplier = 0.1;
        this.stats.damageMultiplier = 0.2;
        this.stats.maxEnergy = 100;
        this.general.favoriteFood = RiftCreatureConfigDefaults.defaultPiscivoreFoods;
        this.general.mobsToRunFrom = Arrays.asList(
                "prift:sarcosuchus"
        );
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
        this.general.blockBreakLevels = Arrays.asList();
        this.spawnRules = Arrays.asList(
                new SpawnRule().setCategory("WATER").setSpawnInWater().setMustSeeSky().setYLevelRange(56, 64).setWeight(25).setSpawnAmntRange(4, 6).setDensityLimit(16).setBiomes("all", "-tag:ocean", "-tag:beach"),
                new SpawnRule().setCategory("CAVE").setSpawnInWater().setYLevelRange(0, 56).setWeight(10).setSpawnAmntRange(4, 6).setDensityLimit(16).setBiomes("all", "-tag:ocean", "-tag:beach")
        );
    }
}
