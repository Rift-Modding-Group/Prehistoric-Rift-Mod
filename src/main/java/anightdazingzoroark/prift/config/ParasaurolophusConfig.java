package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class ParasaurolophusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0;
    public static int parasaurolophusDensityLimit = 12;
    public static String[] parasaurolophusFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] parasaurolophusTamingFood = {"prift:basic_herbivore_meal:0:0.10", "prift:advanced_herbivore_meal:0:33"};
    public static String[] parasaurolophusMineBlock = {
            "minecraft:wheat:7",
            "minecraft:carrots:7",
            "minecraft:potatoes:7",
            "minecraft:beetroots:3",
            "prift:pyroberry_bush:2",
            "prift:pyroberry_bush:3",
            "prift:cryoberry_bush:2",
            "prift:cryoberry_bush:3"
    };
    public static String parasaurolophusSaddleItem = "minecraft:saddle:0";

    public ParasaurolophusConfig(Configuration config) {
        super(config, new String[]{"tag:plains:10:4:6:CREATURE", "-tag:savanna"});
        maxHealth = initMaxHealth = 60;
        damage = initDamage = 0;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        parasaurolophusDensityLimit = config.getInt("Density Limit", "Spawning", 12, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        parasaurolophusFavoriteFood = config.getStringList("Parasaurolophus Favorite Food", "General", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Triceratopses will eat. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        parasaurolophusTamingFood = config.getStringList("Parasaurolophus Taming Food", "General", new String[]{"prift:basic_herbivore_meal:0:0.10", "prift:advanced_herbivore_meal:0:33"}, "List of foods Parasaurolophi must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        parasaurolophusMineBlock = config.getStringList("Parasaurolophus Harvestable Blocks", "General", new String[]{
                "minecraft:wheat:7",
                "minecraft:carrots:7",
                "minecraft:potatoes:7",
                "minecraft:beetroots:3",
                "prift:pyroberry_bush:2",
                "prift:pyroberry_bush:3",
                "prift:cryoberry_bush:2",
                "prift:cryoberry_bush:3"
        }, "List of blocks that Parasaurolophuses when set to harvest on wander will mine. To add items add \"<insert item's identifier here>:<insert data id here>\"");
        parasaurolophusSaddleItem = config.getString("Parasaurolophus Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
