package anightdazingzoroark.prift.config;

import net.minecraftforge.common.config.Configuration;

public class DodoConfig extends RiftConfig {
    private static int maxHealth;
    private static int initMaxHealth;
    public static String[] dodoBreedingFood = {"minecraft:wheat_seeds:0:0", "minecraft:pumpkin_seeds:0:0", "minecraft:melon_seeds:0:0", "minecraft:beetroot_seeds:0:0"};

    public DodoConfig(Configuration config) {
        super(config, new String[]{"tag:plains:15:2:3:CREATURE", "tag:desert:15:2:3:CREATURE", "tag:forest:15:2:3:CREATURE"});
        maxHealth = initMaxHealth = 6;
    }

    @Override
    public void init() {
        super.init();
        maxHealth = config.getInt("Max health for this creature", "Creature Stats", initMaxHealth, 1, 69420, "Maximum health of this creature");

        dodoBreedingFood = config.getStringList("Dodo Breeding Food", "General", new String[]{"minecraft:wheat_seeds:0:0", "minecraft:pumpkin_seeds:0:0", "minecraft:melon_seeds:0:0", "minecraft:beetroot_seeds:0:0"}, "List of foods Dodos need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
    }

    public static double getMaxHealth() {
        return maxHealth;
    }

    public static double getMinHealth() {
        return ((double)maxHealth)/8D;
    }
}
