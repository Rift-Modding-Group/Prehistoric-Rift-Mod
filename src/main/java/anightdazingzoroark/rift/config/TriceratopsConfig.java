package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class TriceratopsConfig extends RiftConfig {
    public static String[] triceratopsFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] triceratopsTamingFood = {"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"};

    public TriceratopsConfig(Configuration config) {
        super(config, new String[]{"biome:minecraft:plains:20:4:6"}, 80, 25);
    }

    @Override
    public void init() {
        super.init();
        triceratopsFavoriteFood = config.getStringList("Triceratops Favorite Food", "General", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Triceratopses will eat. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        triceratopsTamingFood = config.getStringList("Triceratops Taming Food", "General", new String[]{"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"}, "List of foods Triceratopses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
    }
}
