package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class DimetrodonConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static String[] dimetrodonFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"};
    public static String[] dimetrodonTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] dimetrodonTargets = {};
    public static String[] dimetrodonTargetBlacklist = {"minecraft:player", "prift:stegosaurus", "prift:triceratops", "prift:parasaurolophus"};
    public static String[] dimetrodonForcedTemperatureItems = {"prift:extreme_frost_stimulant:0:9600:VERY_COLD", "prift:frost_stimulant:0:9600:COLD", "prift:neutral_stimulant:0:9600:NEUTRAL", "prift:flame_stimulant:0:9600:WARM", "prift:extreme_flame_stimulant:0:9600:VERY_WARM"};

    private final String simpleDiffName = "For Simple Difficulty Integration";
    public static float dimetrodonVeryWarmValue = 10;
    public static float dimetrodonWarmValue = 5;
    public static float dimetrodonVeryColdValue = -10;
    public static float dimetrodonColdValue = -5;

    public DimetrodonConfig(Configuration config) {
        super(config, new String[]{"tag:desert:10:1:1:CREATURE", "tag:savanna:10:1:1:CREATURE"});
        maxHealth = initMaxHealth = 40;
        damage = initDamage = 8;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        dimetrodonFavoriteFood = config.getStringList("Dimetrodon Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"}, "List of foods Dimetrodons will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        dimetrodonTamingFood = config.getStringList("Dimetrodon Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Dimetrodons must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        dimetrodonTargets = config.getStringList("Dimetrodon Targets", "General", new String[]{}, "Identifiers of mobs that the Dimetrodon will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        dimetrodonTargetBlacklist = config.getStringList("Dimetrodon Target Blacklist", "General", new String[]{"minecraft:player", "prift:stegosaurus", "prift:triceratops", "prift:parasaurolophus"}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Dimetrodons.");
        dimetrodonForcedTemperatureItems = config.getStringList("Dimetrodon Temperature Changing Items", "General", new String[]{"prift:extreme_frost_stimulant:0:9600:VERY_COLD", "prift:frost_stimulant:0:9600:COLD", "prift:neutral_stimulant:0:9600:NEUTRAL", "prift:flame_stimulant:0:9600:WARM", "prift:extreme_flame_stimulant:0:9600:VERY_WARM"}, "List of items that can forcibly change the temperature mode of a Dimetrodon. To add items add \"<insert item's identifier here>:<insert data id here>:<insert time in ticks here>:<insert temperature mode here>\". Valid temperature mods are VERY_COLD, COLD, NEUTRAL, WARM, and VERY_WARM");

        dimetrodonVeryWarmValue = config.getFloat("\"Very Warm\" temperature value", this.simpleDiffName, 10f, 0, 69420f, "Temperature strength of Dimetrodons in \"Very Warm\" mode");
        dimetrodonWarmValue = config.getFloat("\"Warm\" temperature value", this.simpleDiffName, 5f, 0, 69420f, "Temperature strength of Dimetrodons in \"Warm\" mode");
        dimetrodonVeryColdValue = config.getFloat("\"Very Cold\" temperture value", this.simpleDiffName, -10f, -69420f, 0, "Temperature strength of Dimetrodons in \"Very Cold\" mode");
        dimetrodonColdValue = config.getFloat("\"Cold\" temperture value", this.simpleDiffName, -5f, -69420f, 0, "Temperature strength of Dimetrodons in \"Cold\" mode");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
