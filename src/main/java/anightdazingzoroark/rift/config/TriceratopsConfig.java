package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class TriceratopsConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] triceratopsFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] triceratopsTamingFood = {"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"};

    public TriceratopsConfig(Configuration config) {
        super(config, new String[]{"tag:plains:10:4:6", "-tag:savanna"});
        maxHealth = initMaxHealth = 80;
        damage = initDamage = 25;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        triceratopsFavoriteFood = config.getStringList("Triceratops Favorite Food", "General", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Triceratopses will eat. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        triceratopsTamingFood = config.getStringList("Triceratops Taming Food", "General", new String[]{"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"}, "List of foods Triceratopses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
