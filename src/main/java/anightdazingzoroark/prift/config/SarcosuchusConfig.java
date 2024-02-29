package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class SarcosuchusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] sarcosuchusFavoriteFood = {"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:rotten_flesh:0:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"};
    public static String[] sarcosuchusTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] sarcosuchusTargets = {"prift:megapiranha"};
    public static String[] sarcosuchusTargetBlacklist = {};
    public static String sarcosuchusSaddleItem = "minecraft:saddle:0";
    public static String[] sarcosuchusSpinBlacklist = {"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus", "prift:sarcosuchus"};
    public static boolean sarcosuchusSpinWhitelist = false;

    public SarcosuchusConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:river:10:1:1:WATER_CREATURE", "tag:swamp:15:1:1:WATER_CREATURE"});
        maxHealth = initMaxHealth = 80;
        damage = initDamage = 15;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420666, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420666, "Maximum (melee) damage of this creature");

        sarcosuchusFavoriteFood = config.getStringList("Sarcosuchus Favorite Food", "General", new String[]{"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:rotten_flesh:0:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"}, "List of foods Sarcosuchuses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        sarcosuchusTamingFood = config.getStringList("Sarcosuchus Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Sarcosuchuses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        sarcosuchusTargets = config.getStringList("Sarcosuchus Targets", "General", new String[]{"prift:megapiranha"}, "Identifiers of mobs that the Sarcosuchus will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        sarcosuchusTargetBlacklist = config.getStringList("Sarcosuchus Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Sarcosuchuses.");
        sarcosuchusSaddleItem = config.getString("Sarcosuchus Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
        sarcosuchusSpinBlacklist = config.getStringList("Mobs immune to Sarcosuchus spin attack", "General", new String[]{"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus", "prift:sarcosuchus"}, "Sarcosuchuses cannot use their spin attack on mobs whose identifiers are in this list");
        sarcosuchusSpinWhitelist = config.getBoolean("Use Sarcosuchus spin target blacklist as whitelist", "General", false, "Turn the blacklist of mobs the Sarcosuchus can use its spin attack on into a whitelist");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
