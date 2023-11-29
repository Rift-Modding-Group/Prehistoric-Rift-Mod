package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class DodoConfig extends RiftConfig {
    public static String[] dodoBreedingFood = {"minecraft:wheat_seeds:0:0", "minecraft:pumpkin_seeds:0:0", "minecraft:melon_seeds:0:0", "minecraft:beetroot_seeds:0:0"};

    public DodoConfig(Configuration config) {
        super(config, new String[]{"tag:plains:15:2:3", "tag:desert:15:2:3", "tag:forest:15:2:3"}, 6, 0);
    }

    @Override
    public void init() {
        super.init();
        dodoBreedingFood = config.getStringList("Dodo Breeding Food", "General", new String[]{"minecraft:wheat_seeds:0:0", "minecraft:pumpkin_seeds:0:0", "minecraft:melon_seeds:0:0", "minecraft:beetroot_seeds:0:0"}, "List of foods Dodos need to be fed in order to breed. To add items add \"<insert item's identifier here>:<insert data id here>:0\"");
    }
}
