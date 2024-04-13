package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class TyrannosaurusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static double healthMultiplier = 0.1;
    public static double damageMultiplier = 0.5;
    public static int tyrannosaurusDensityLimit = 4;
    public static String[] tyrannosaurusFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"};
    public static String[] tyrannosaurusBreedingFood = {"prift:basic_carnivore_meal:0:0", "prift:advanced_carnivore_meal:0:0"};
    public static String[] tyrannosaurusTargets = {"prift:apatosaurus"};
    public static String[] tyrannosaurusTargetBlacklist = {};
    public static boolean tyrannosaurusRoarTargetsWhitelist = false;
    public static String[] tyrannosaurusRoarTargetBlacklist = {"prift:tyrannosaurus", "prift:apatosaurus"};
    public static String tyrannosaurusSaddleItem = "minecraft:saddle:0";

    public TyrannosaurusConfig(Configuration config) {
        super(config, new String[]{"tag:plains:7:1:1:CREATURE", "tag:mountain:10:1:1:CREATURE", "-tag:savanna", "-tag:cold"});
        maxHealth = initMaxHealth = 160;
        damage = initDamage = 35;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt(RiftConfig.healthConfigName, "Creature Stats", initMaxHealth, 1, 69420666, RiftConfig.healthConfigMessage);
        damage = config.getInt(RiftConfig.damageConfigName, "Creature Stats", initDamage, 0, 69420666, RiftConfig.damageConfigMessage);
        healthMultiplier = config.getFloat(RiftConfig.healthMultiplierConfigName, "Creature Stats", 0.1f, 0, 1f, RiftConfig.healthMultiplierConfigMessage);
        damageMultiplier = config.getFloat(RiftConfig.damageMultiplierConfigName, "Creature Stats", 0.5f, 0, 1f, RiftConfig.damageMultiplierConfigMessage);

        tyrannosaurusDensityLimit = config.getInt("Density Limit", "Spawning", 4, 1, 69420666, "Maximum amount of creatures of this type in a 64 x 64 x 64 area");

        tyrannosaurusFavoriteFood = config.getStringList("Tyrannosaurus Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "prift:raw_exotic_meat:0:0.05", "prift:cooked_exotic_meat:0:0.075", "prift:raw_fibrous_meat:0:0", "prift:cooked_fibrous_meat:0:0", "prift:raw_hadrosaur_meat:0:0.05", "prift:cooked_hadrosaur_meat:0:0.075"}, "List of foods Tyrannosauruses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        tyrannosaurusBreedingFood = config.getStringList("Tyrannosaurus Breeding Food", "General", new String[]{"prift:basic_carnivore_meal:0:0", "prift:advanced_carnivore_meal:0:0"}, "List of foods Tyrannosauruses need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        tyrannosaurusTargets = config.getStringList("Tyrannosaurus Targets", "General", new String[]{"prift:apatosaurus"}, "Identifiers of mobs that the Tyrannosaurus will actively hunt, alongside the ones defined in the general config for all carnivores to target");
        tyrannosaurusTargetBlacklist = config.getStringList("Tyrannosaurus Target Blacklist", "General", new String[]{}, "Identifiers of mobs that are here, if they are in the general config for all carnivores to target, will not be targeted by Tyrannosauruses.");
        tyrannosaurusRoarTargetsWhitelist = config.getBoolean("Use Tyrannosaurus roar blacklist as a whitelist", "General", false, "Turn the blacklist of mobs affected by the Tyrannosaurus' roar into a whitelist of affected mobs");
        tyrannosaurusRoarTargetBlacklist = config.getStringList("Mobs unaffected by Tyrannosaurus roar", "General", new String[]{"prift:tyrannosaurus", "prift:apatosaurus"}, "Mobs within this list are immune to this mobs knockback roar");
        tyrannosaurusSaddleItem = config.getString("Tyrannosaurus Saddle Item", "General", "minecraft:saddle:0", "Item that counts as a saddle for this creature. To add an item add \"<insert item's identifier here>:<insert data id here>\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
