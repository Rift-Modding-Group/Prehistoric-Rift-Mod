package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class ApatosaurusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] apatosaurusFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] apatosaurusBreedingFood = {"prift:basic_herbivore_meal:0:0", "prift:advanced_herbivore_meal:0:0"};

    public ApatosaurusConfig(Configuration config) {
        super(config, new String[]{"tag:plains:7:1:3", "-tag:savanna"});
        maxHealth = initMaxHealth = 200;
        damage = initDamage = 80;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        apatosaurusFavoriteFood = config.getStringList("Apatosaurus Favorite Food", "General", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Apatosauruses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        apatosaurusBreedingFood = config.getStringList("Apatosaurus Breeding Food", "General", new String[]{"prift:basic_herbivore_meal:0:0", "prift:advanced_herbivore_meal:0:0"}, "List of foods Apatosauruses need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
