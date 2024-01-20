package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class ParasaurolophusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] parasaurolophusFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] parasaurolophusTamingFood = {"prift:basic_herbivore_meal:0:0.10", "prift:advanced_herbivore_meal:0:33"};
    public static String[] parasaurolophusTargets = {"minecraft:zombie", "minecraft:zombie_villager", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider", "minecraft:cave_spider", "minecraft:silverfish", "minecraft:husk", "minecraft:stray", "minecraft:slime", "minecraft:vex", "minecraft:zombie_pigman", "minecraft:ghast", "minecraft:blaze", "minecraft:wither_skeleton", "minecraft:magma_cube", "minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:utahraptor"};
    public static String parasaurolophusSaddleItem = "minecraft:saddle:0";
    public static String[] parasaurolophusScareBlacklist = {"minecraft:villager", "minecraft:enderman", "minecraft:witch", "minecraft:vindicator", "minecraft:evoker", "minecraft:vex", "minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus"};
    public static boolean parasaurolophusScareBlistAsWlist = false;

    public ParasaurolophusConfig(Configuration config) {
        super(config, new String[]{"tag:plains:10:4:6", "-tag:savanna"});
        maxHealth = initMaxHealth = 60;
        damage = initDamage = 0;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        parasaurolophusFavoriteFood = config.getStringList("Parasaurolophus Favorite Food", "General", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Triceratopses will eat. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        parasaurolophusTamingFood = config.getStringList("Parasaurolophus Taming Food", "General", new String[]{"prift:basic_herbivore_meal:0:0.10", "prift:advanced_herbivore_meal:0:33"}, "List of foods Triceratopses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        parasaurolophusTargets = config.getStringList("Mobs wild Parasaurolophi will run from", "General", new String[]{"minecraft:zombie", "minecraft:zombie_villager", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider", "minecraft:cave_spider", "minecraft:silverfish", "minecraft:husk", "minecraft:stray", "minecraft:slime", "minecraft:vex", "minecraft:zombie_pigman", "minecraft:ghast", "minecraft:blaze", "minecraft:wither_skeleton", "minecraft:magma_cube", "minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:utahraptor"}, "Identifiers of mobs wild Parasaurolophi will run from");
        parasaurolophusSaddleItem = config.getString("Parasaurolophus Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
        parasaurolophusScareBlacklist = config.getStringList("Mobs tamed Parasaurolophi cannot scare off", "General", new String[]{"minecraft:villager", "minecraft:enderman", "minecraft:witch", "minecraft:vindicator", "minecraft:evoker", "minecraft:vex", "minecraft:ender_dragon", "minecraft:wither", "prift:tyrannosaurus", "prift:stegosaurus", "prift:triceratops", "prift:apatosaurus"}, "Identifiers of mobs tamed Parasaurolophi cannot scare off");
        parasaurolophusScareBlistAsWlist = config.getBoolean("Use scare off list as whitelist", "General", false, "Set true to turn the blacklist of mobs tamed Parasaurolophi can scare into a whitelist");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
