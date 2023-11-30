package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class TyrannosaurusConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static int damage;
    private static int initDamage;
    public static String[] tyrannosaurusFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "rift:raw_exotic_meat:0:0.05", "rift:cooked_exotic_meat:0:0.075", "rift:raw_fibrous_meat:0:0", "rift:cooked_fibrous_meat:0:0"};
    public static String[] tyrannosaurusBreedingFood = {"rift:basic_carnivore_meal:0:0", "rift:advanced_carnivore_meal:0:0"};
    public static String[] tyrannosaurusTargets = {"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama", "rift:stegosaurus", "rift:dodo", "rift:triceratops"};
    public static boolean tyrannosaurusRoarTargetsWhitelist = false;
    public static String[] tyrannosaurusRoarTargetBlacklist = {"rift:tyrannosaurus"};

    public TyrannosaurusConfig(Configuration config) {
        super(config, new String[]{"tag:plains:7:1:1", "tag:mountain:10:1:1", "-tag:savanna", "-tag:cold"});
        maxHealth = initMaxHealth = 160;
        damage = initDamage = 35;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");
        damage = config.getInt("Max damage for this creature", "Creature Stats", initDamage, 0, 69420, "Maximum (melee) damage of this creature");

        tyrannosaurusFavoriteFood = config.getStringList("Tyrannosaurus Favorite Food", "General", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "rift:raw_exotic_meat:0:0.05", "rift:cooked_exotic_meat:0:0.075", "rift:raw_fibrous_meat:0:0", "rift:cooked_fibrous_meat:0:0"}, "List of foods Tyrannosauruses will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        tyrannosaurusBreedingFood = config.getStringList("Tyrannosaurus Breeding Food", "General", new String[]{"rift:basic_carnivore_meal:0:0", "rift:advanced_carnivore_meal:0:0"}, "List of foods Tyrannosauruses need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
        tyrannosaurusTargets = config.getStringList("Tyrannosaurus Targets", "General", new String[]{"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama", "rift:stegosaurus", "rift:dodo", "rift:triceratops"}, "Identifiers of mobs that the Tyrannosaurus will actively hunt");
        tyrannosaurusRoarTargetsWhitelist = config.getBoolean("Use Tyrannosaurus roar blacklist as a whitelist", "General", false, "Turn the blacklist of mobs affected by the Tyrannosaurus' roar into a whitelist of affected mobs");
        tyrannosaurusRoarTargetBlacklist = config.getStringList("Mobs unaffected by Tyrannosaurus roar", "General", new String[]{"rift:tyrannosaurus"}, "Mobs within this list are immune to this mobs knockback roar");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
