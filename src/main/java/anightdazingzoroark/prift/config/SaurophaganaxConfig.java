package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class SaurophaganaxConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int saurophaganaxDensityLimit = 1;
    public static String[] saurophaganaxFavoriteFood = {"minecraft:rotten_flesh:0:0.05", "minecraft:bone:0:0.05", "minecraft:gunpowder:0:0.05", "minecraft:spider_eye:0:0.05", "minecraft:fermented_spider_eye:0:0.025", "minecraft:slime_ball:0:0.05", "minecraft:ender_pearl:0:0.05", "minecraft:ghast_tear:0:0.05", "minecraft:blaze_rod:0:0.05", "minecraft:blaze_powder:0:0.025", "minecraft:magma_cream:0:0.05", "prift:raw_hemolymph:0:0.025", "prift:cooked_hemolymph:0:0.05"};
    public static String[] saurophaganaxTamingFood = {"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"};
    public static String[] saurophaganaxTargets = {"minecraft:cave_spider", "minecraft:enderman", "minecraft:spider", "minecraft:zombie_pigman", "minecraft:blaze", "minecraft:creeper", "minecraft:elder_guardian", "minecraft:endermite", "minecraft:ghast", "minecraft:guardian", "minecraft:husk", "minecraft:magma_cube", "minecraft:shulker", "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:zombie", "minecraft:zombie_villager"};
    public static String saurophaganaxSaddleItem = "minecraft:saddle:0";

    public SaurophaganaxConfig(Configuration config) {
        super(config, new String[]{"all:90:1:1:MONSTER"});
        maxHealth = initMaxHealth = 100;
        damage = initDamage = 60;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        saurophaganaxDensityLimit = config.getInt("Density Limit", "Spawning", 1, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        saurophaganaxFavoriteFood = config.getStringList("Saurophaganax Favorite Food", "General", new String[]{"minecraft:rotten_flesh:0:0.05", "minecraft:bone:0:0.05", "minecraft:gunpowder:0:0.05", "minecraft:spider_eye:0:0.05", "minecraft:fermented_spider_eye:0:0.025", "minecraft:slime_ball:0:0.05", "minecraft:ender_pearl:0:0.05", "minecraft:ghast_tear:0:0.05", "minecraft:blaze_rod:0:0.05", "minecraft:blaze_powder:0:0.025", "minecraft:magma_cream:0:0.05", "prift:raw_hemolymph:0:0.025", "prift:cooked_hemolymph:0:0.05"}, "List of foods Saurophaganaxes will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        saurophaganaxTamingFood = config.getStringList("Saurophaganax Taming Food", "General", new String[]{"prift:basic_carnivore_meal:0:0.10", "prift:advanced_carnivore_meal:0:0.33"}, "List of foods Saurophaganaxes must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        saurophaganaxTargets = config.getStringList("Saurophaganax Targets", "General", new String[]{"minecraft:cave_spider", "minecraft:enderman", "minecraft:spider", "minecraft:zombie_pigman", "minecraft:blaze", "minecraft:creeper", "minecraft:elder_guardian", "minecraft:endermite", "minecraft:ghast", "minecraft:guardian", "minecraft:husk", "minecraft:magma_cube", "minecraft:shulker", "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:zombie", "minecraft:zombie_villager"}, "Identifiers of mobs that the Saurophaganax will actively hunt");
        saurophaganaxSaddleItem = config.getString("Saurophaganax Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
