package anightdazingzoroark.rift.config;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class StegosaurusConfig extends RiftConfig {
    public static String[] stegosaurusFavoriteFood = {"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"};
    public static String[] stegosaurusTamingFood = {"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"};
    public static boolean stegosaurusCanInflictBleed = true;

    public StegosaurusConfig(Configuration config) {
        super(config);
    }

    @Override
    public void init() {
        stegosaurusFavoriteFood = config.getStringList("Stegosaurus Favorite Food", "Stegosaurus", new String[]{"minecraft:apple:0:0.025", "minecraft:wheat:0:0.05", "minecraft:carrot:0:0.05", "minecraft:potato:0:0.05", "minecraft:beetroot:0:0.05"}, "List of foods Stegosauruses will eat. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        stegosaurusTamingFood = config.getStringList("Stegosaurus Taming Food", "Stegosaurus", new String[]{"rift:basic_herbivore_meal:0:0.10", "rift:advanced_herbivore_meal:0:33"}, "List of foods Stegosauruses must be fed to be tamed (if wild) or bred (if tamed). To add items add \"<insert item's identifier here>:<insert data id here>:<percentage of tame progress to fill up before taming>\"");
        stegosaurusCanInflictBleed = config.getBoolean("Stegosaurus Can Inflict Bleed", "Stegosaurus", true, "Whether or not Stegosauruses can inflict bleed using their strong attack");
    }
}
