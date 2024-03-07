package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class MegapiranhaConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.2;
    public static String[] megapiranhaFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"};
    public static String[] megapiranhaTargets = {};
    public static String[] megapiranhaTargetBlacklist = {};

    public MegapiranhaConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:river:15:4:6:WATER_CREATURE"});
        maxHealth = initMaxHealth = 4;
        damage = initDamage = 2;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.2f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        megapiranhaFavoriteFood = config.getStringList("Megapiranha Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"}, "List of foods Megapiranhas will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        megapiranhaTargets = config.getStringList("Megapiranha Targets", "General", new String[]{}, "Identifiers of mobs that the Megapiranha will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        megapiranhaTargetBlacklist = config.getStringList("Megapiranha Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Megapiranhas.");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
