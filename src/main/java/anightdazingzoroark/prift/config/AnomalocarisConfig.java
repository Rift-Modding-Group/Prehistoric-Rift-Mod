package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class AnomalocarisConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int anomalocarisDensityLimit = 4;
    public static String[] anomalocarisFavoriteFood = {"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"};
    public static String[] anomalocarisTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] anomalocarisTargets = {"minecraft:squid", "prift:megapiranha"};
    public static String[] anomalocarisTargetBlacklist = {};
    public static String anomalocarisSaddleItem = "minecraft:saddle:0";
    public static String[] anomalocarisGrabBlacklist = {"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus", "prift:anomalocaris"};
    public static boolean anomalocarisGrabWhitelist = false;

    public AnomalocarisConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:deep_ocean:5:1:1:WATER_CREATURE"});
        maxHealth = initMaxHealth = 50;
        damage = initDamage = 5;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        anomalocarisDensityLimit = config.getInt("Density Limit", "Spawning", 4, 1, 69620666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        anomalocarisFavoriteFood = config.getStringList("Anomalocaris Favorite Food", "General", new String[]{"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"}, "List of foods Anomalocarises will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        anomalocarisTamingFood = config.getStringList("Anomalocaris Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Anomalocarises must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        anomalocarisTargets = config.getStringList("Anomalocaris Targets", "General", new String[]{"minecraft:squid", "prift:megapiranha"}, "Identifiers of mobs that the Anomalocaris will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        anomalocarisTargetBlacklist = config.getStringList("Anomalocaris Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Anomalocarises.");
        anomalocarisSaddleItem = config.getString("Anomalocaris Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
        anomalocarisGrabBlacklist = config.getStringList("Mobs immune to Anomalocaris grab", "General", new String[]{"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus", "prift:anomalocaris"}, "Anomalocarises cannot grab mobs whose identifiers are in this list");
        anomalocarisGrabWhitelist = config.getBoolean("Use Anomalocaris grab blacklist as whitelist", "General", false, "Turn the blacklist of mobs the Anomalocaris can grab into a whitelist");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
