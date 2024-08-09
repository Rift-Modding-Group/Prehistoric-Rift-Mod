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
        this.spawnRules.spawnAmntRange = Arrays.asList(4, 6);
        this.spawnRules.densityLimit = 16;
        this.spawnRules.spawnType = "WATER";
        this.spawnRules.spawnBiomes = Arrays.asList(
                new SpawnBiomes(Arrays.asList("all", "-tag:ocean", "-tag:beach"), 25)
        );
    }
}
