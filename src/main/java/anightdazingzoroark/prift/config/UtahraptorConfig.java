package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class UtahraptorConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] utahraptorFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0"};
    public static String[] utahraptorTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] utahraptorTargets = {};
    public static String[] utahraptorTargetBlacklist = {};
    public static String utahraptorSaddleItem = "minecraft:saddle:0";

    public UtahraptorConfig(Configuration config) {
        super(config, new String[]{"tag:plains:10:2:4", "tag:forest:15:2:4", "tag:jungle:15:2:4", "-tag:savanna", "-tag:cold"});
        maxHealth = initMaxHealth = 30;
        damage = initDamage = 6;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        utahraptorFavoriteFood = config.getStringList("Utahraptor Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0"}, "List of foods Utahraptors will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        utahraptorTamingFood = config.getStringList("Utahraptor Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Utahraptors must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        utahraptorTargets = config.getStringList("Utahraptor Targets", "General", new String[]{}, "Identifiers of mobs that the Utahraptor will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        utahraptorTargetBlacklist = config.getStringList("Utahraptor Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not bye targeted by Utahraptors.");
        utahraptorSaddleItem = config.getString("Utahraptor Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
