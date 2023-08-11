package anightdazingzoroark.rift;

import anightdazingzoroark.rift.server.ServerProxy;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class RiftConfig {
    public static int tyrannosaurusSpawnWeight = 6;
    public static String[] tyrannosaurusSpawnBiomes = {"minecraft:plains", "minecraft:extreme_hills", "minecraft:smaller_extreme_hills"};
    public static String[] tyrannosaurusFavoriteFood = {"minecraft:beef", "minecraft:cooked_beef", "minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:chicken", "minecraft:cooked_chicken", "minecraft:mutton", "minecraft:cooked_mutton", "minecraft:rabbit", "minecraft:cooked_rabbit", "minecraft:rotten_flesh", "rift:raw_exotic_meat", "rift:cooked_exotic_meat"};
    public static String[] tyrannosaurusTargets = {"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule"};

    public static void readConfig() {
        Configuration cfg = RiftInitialize.config;
        try {
            cfg.load();
            init(cfg);
        }
        catch (Exception e1) {
            RiftInitialize.logger.log(Level.ERROR, "Problem loading config file!", e1);
        }
        finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    public static void init(Configuration config) {
        tyrannosaurusSpawnWeight = config.getInt("Tyrannosaurus Spawn Weight", "Tyrannosaurus", 6, 1, 100000000, "Spawn weight of Tyrannosaurus. Higher value = more common");
        tyrannosaurusSpawnBiomes = config.getStringList("Tyrannosaurus Spawn Biomes", "Tyrannosaurus", new String[]{"minecraft:plains", "minecraft:extreme_hills", "minecraft:smaller_extreme_hills"}, "List of biomes Tyrannosauruses will spawn in");
        tyrannosaurusFavoriteFood = config.getStringList("Tyrannosaurus Favorite Food", "Tyrannosaurus", new String[]{"minecraft:beef", "minecraft:cooked_beef", "minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:chicken", "minecraft:cooked_chicken", "minecraft:mutton", "minecraft:cooked_mutton", "minecraft:rabbit", "minecraft:cooked_rabbit", "minecraft:rotten_flesh", "rift:raw_exotic_meat", "rift:cooked_exotic_meat"}, "List of foods Tyrannosaurus will eat (when tamed) or pick up when on the ground");
        tyrannosaurusTargets = config.getStringList("Tyrannosaurus Targets", "Tyrannosaurus", new String[]{"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule"}, "Identifiers of mobs that the Tyrannosaurus will actively hunt");
    }
}
