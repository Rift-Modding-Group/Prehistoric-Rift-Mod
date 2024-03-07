package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class CoelacanthConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static double healthMultiplier = 0.1;

    public CoelacanthConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:deep_ocean:15:4:6:WATER_CREATURE"});
        maxHealth = initMaxHealth = 6;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
