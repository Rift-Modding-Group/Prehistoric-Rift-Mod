package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class UtahraptorConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int utahraptorDensityLimit = 16;
    public static String[] utahraptorFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"};
    public static String[] utahraptorTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] utahraptorTargets = {};
    public static String[] utahraptorTargetBlacklist = {};
    public static String utahraptorSaddleItem = "minecraft:saddle:0";

    public UtahraptorConfig(Configuration config) {
        super(config, new String[]{"tag:plains:10:2:4:CREATURE", "tag:forest:15:2:4:CREATURE", "tag:jungle:15:2:4:CREATURE", "-tag:savanna", "-tag:cold"});
        maxHealth = initMaxHealth = 30;
        damage = initDamage = 6;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        utahraptorDensityLimit = config.getInt("Density Limit", "Spawning", 16, 1, 69620666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        utahraptorFavoriteFood = config.getStringList("Utahraptor Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"}, "List of foods Utahraptors will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        utahraptorTamingFood = config.getStringList("Utahraptor Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Utahraptors must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        utahraptorTargets = config.getStringList("Utahraptor Targets", "General", new String[]{}, "Identifiers of mobs that the Utahraptor will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        utahraptorTargetBlacklist = config.getStringList("Utahraptor Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Utahraptors.");
        utahraptorSaddleItem = config.getString("Utahraptor Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
