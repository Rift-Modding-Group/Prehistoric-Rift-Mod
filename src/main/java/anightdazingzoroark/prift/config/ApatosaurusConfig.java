package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class ApatosaurusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] apatosaurusFavoriteFood = {"minecraft:leaves:-1:0.025", "minecraft:leaves2:-1:0.025"};
    public static String[] apatosaurusBreedingFood = {"prift:basic_herbivore_meal:0:0", "prift:advanced_herbivore_meal:0:0"};
    public static String apatosaurusSaddleItem = "prift:apatosaurus_platform:0";
    public static boolean apatosaurusPassengerWhitelist = false;
    public static String[] apatosaurusPassengerBlacklist = {"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:apatosaurus"};
//    public static boolean apatosaurusCanBreakBlocks = true;

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

        apatosaurusFavoriteFood = config.getStringList("Apatosaurus Favorite Food", "General", new String[]{"minecraft:leaves:-1:0.025", "minecraft:leaves2:-1:0.025"}, "List of foods Apatosauruses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        apatosaurusBreedingFood = config.getStringList("Apatosaurus Breeding Food", "General", new String[]{"prift:basic_herbivore_meal:0:0", "prift:advanced_herbivore_meal:0:0"}, "List of foods Apatosauruses need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        apatosaurusSaddleItem = config.getString("Apatosaurus Saddle Item", "General", "prift:apatosaurus_platform:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
        apatosaurusPassengerWhitelist = config.getBoolean("Use Apatosaurus passenger blacklist as a whitelist", "General", false, "Turn the blacklist of mobs that can be made to forcefully ride an Apatosaurus (with a platform) into a whitelist");
        apatosaurusPassengerBlacklist = config.getStringList("Mobs that not can be passengers for the Apatosaurus", "General", new String[]{"minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:apatosaurus"}, "Mobs within this list cannot be passengers on the back of an Apatosaurus (with a platform)");
//        apatosaurusCanBreakBlocks = config.getBoolean("Apatosaurus can break blocks", "General", true, "If true, this mob can break blocks weaker than wood just by walking through them.");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
