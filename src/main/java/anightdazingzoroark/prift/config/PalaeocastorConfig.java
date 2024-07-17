package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class PalaeocastorConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.2;
    public static int palaeocastorDensityLimit = 4;
    public static String[] palaeocastorFavoriteFood = {"minecraft:coal:0:0.05", "minecraft:cobblestone:0:0.025", "minecraft:stone:0:0.05", "minecraft:stone:1:0.05", "minecraft:stone:2:0.2", "minecraft:stone:3:0.05", "minecraft:stone:4:0.2", "minecraft:stone:5:0.05", "minecraft:stone:6:0.2"};
    public static String[] palaeocastorTamingFood = {"prift:basic_saxumavore_meal:0:0.10", "prift:advanced_saxumavore_meal:0:0.33"};

    public PalaeocastorConfig(Configuration config) {
        super(config, new String[]{"all:10:1:1:CREATURE"});
        maxHealth = initMaxHealth = 6;
        damage = initDamage = 4;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.2f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        palaeocastorDensityLimit = config.getInt("Density Limit", "Spawning", 4, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        palaeocastorFavoriteFood = config.getStringList("Palaeocastor Favorite Food", "General", new String[]{"minecraft:coal:0:0.05", "minecraft:cobblestone:0:0.025", "minecraft:stone:0:0.05", "minecraft:stone:1:0.05", "minecraft:stone:2:0.2", "minecraft:stone:3:0.05", "minecraft:stone:4:0.2", "minecraft:stone:5:0.05", "minecraft:stone:6:0.2"}, "List of foods Palaeocastors will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        palaeocastorTamingFood = config.getStringList("Palaeocastor Taming Food", "General", new String[]{"prift:basic_saxumavore_meal:0:0.10", "prift:advanced_saxumavore_meal:0:0.33"}, "List of foods Palaeocastors must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
