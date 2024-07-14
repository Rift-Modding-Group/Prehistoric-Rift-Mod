package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class BaryonyxConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int baryonyxDensityLimit = 2;
    public static String[] baryonyxFavoriteFood = {"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"};
    public static String[] baryonyxTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] baryonyxTargets = {"minecraft:squid", "prift:megapiranha"};
    public static String[] baryonyxTargetBlacklist = {};
    public static String baryonyxSaddleItem = "minecraft:saddle:0";

    public BaryonyxConfig(Configuration config) {
        super(config, new String[]{"tag:swamp:10:1:1:WATER_CREATURE"});
        maxHealth = initMaxHealth = 60;
        damage = initDamage = 10;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        baryonyxDensityLimit = config.getInt("Density Limit", "Spawning", 2, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        baryonyxFavoriteFood = config.getStringList("Baryonyx Favorite Food", "General", new String[]{"minecraft:beef:0:0.025", "minecraft:cooked_beef:0:0.05", "minecraft:porkchop:0:0.025", "minecraft:cooked_porkchop:0:0.05", "minecraft:chicken:0:0.025", "minecraft:cooked_chicken:0:0.05", "minecraft:mutton:0:0.025", "minecraft:cooked_mutton:0:0.05", "minecraft:rabbit:0:0.025", "minecraft:cooked_rabbit:0:0.05", "minecraft:fish:0:0.05", "minecraft:cooked_fish:0:0.075", "minecraft:fish:1:0.05", "minecraft:cooked_fish:1:0.075","minecraft:fish:2:0.05", "prift:raw_exotic_meat:0:0.025", "prift:cooked_exotic_meat:0:0.05", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.025", "prift:cooked_hadrosaur_meat:0:0.05", "prift:raw_megapiranha:0:0.05", "prift:cooked_megapiranha:0:0.075"}, "List of foods Baryonyxes will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        baryonyxTamingFood = config.getStringList("Baryonyx Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Baryonyxes must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        baryonyxTargets = config.getStringList("Baryonyx Targets", "General", new String[]{"minecraft:squid", "prift:megapiranha"}, "Identifiers of mobs that the Baryonyx will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        baryonyxTargetBlacklist = config.getStringList("Baryonyx Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Baryonyxes.");
        baryonyxSaddleItem = config.getString("Baryonyx Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
