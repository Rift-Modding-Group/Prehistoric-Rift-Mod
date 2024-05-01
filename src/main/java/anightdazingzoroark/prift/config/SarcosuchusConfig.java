package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class SarcosuchusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int sarcosuchusDensityLimit = 2;
    public static String[] sarcosuchusFavoriteFood = {"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"};
    public static String[] sarcosuchusTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] sarcosuchusTargets = {"minecraft:squid", "prift:megapiranha"};
    public static String[] sarcosuchusTargetBlacklist = {};
    public static String sarcosuchusSaddleItem = "minecraft:saddle:0";
    public static String sarcosuchusSpinMaxSize = "MEDIUM";

    public SarcosuchusConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:river:10:1:1:WATER_CREATURE", "tag:swamp:15:1:1:WATER_CREATURE"});
        maxHealth = initMaxHealth = 80;
        damage = initDamage = 15;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        sarcosuchusDensityLimit = config.getInt("Density Limit", "Spawning", 2, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        sarcosuchusFavoriteFood = config.getStringList("Sarcosuchus Favorite Food", "General", new String[]{"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"}, "List of foods Sarcosuchuses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        sarcosuchusTamingFood = config.getStringList("Sarcosuchus Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Sarcosuchuses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        sarcosuchusTargets = config.getStringList("Sarcosuchus Targets", "General", new String[]{"minecraft:squid", "prift:megapiranha"}, "Identifiers of mobs that the Sarcosuchus will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        sarcosuchusTargetBlacklist = config.getStringList("Sarcosuchus Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Sarcosuchuses.");
        sarcosuchusSaddleItem = config.getString("Sarcosuchus Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
        sarcosuchusSpinMaxSize = config.getString("Maximum size that the Sarcosuchus can use its spin attack on", "General", "MEDIUM", "Maximum size for creatures that can be affected by the Sarcosuchus's spin attack. Accepted values are 'VERY_SMALL', 'SMALL', 'MEDIUM', 'LARGE', and 'VERY_LARGE'");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
