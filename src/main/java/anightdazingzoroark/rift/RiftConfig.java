package anightdazingzoroark.rift;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class RiftConfig {
    public static String[] weakerThanWood = {"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"};
    public static boolean apexAffectedWhitelist = false;
    public static String[] apexAffectedBlacklist = {};

    public static int tyrannosaurusSpawnWeight = 6;
    public static String[] tyrannosaurusSpawnBiomes = {"minecraft:plains", "minecraft:extreme_hills", "minecraft:smaller_extreme_hills"};
    public static String[] tyrannosaurusFavoriteFood = {"minecraft:beef", "minecraft:cooked_beef", "minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:chicken", "minecraft:cooked_chicken", "minecraft:mutton", "minecraft:cooked_mutton", "minecraft:rabbit", "minecraft:cooked_rabbit", "minecraft:rotten_flesh", "rift:raw_exotic_meat", "rift:cooked_exotic_meat"};
    public static String[] tyrannosaurusTargets = {"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama"};
    public static boolean tyrannosaurusRoarTargetsWhitelist = false;
    public static String[] tyrannosaurusRoarTargetBlacklist = {"rift:tyrannosaurus"};

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
        weakerThanWood = config.getStringList("List of Ore Dictionary tags or Blocks weaker than wood", "Misc", new String[]{"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"}, "Blocks and Ore Dictionary tags here will be considered weaker than wood, making them breakable by the Tyrannosaurus' roar and the like. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<inert block's identifier here>\"");
        apexAffectedWhitelist = config.getBoolean("Use Apex effects blacklist as a whitelist", "Misc", false, "Turn the blacklist of mobs affected by an apex predator's special effects into a whitelist of affected mobs");
        apexAffectedBlacklist = config.getStringList("Mobs unaffected by apex predator's effects", "Misc", new String[]{}, "Apex predators can apply some nasty debuffs to nearby mobs, like weakness for the Tyrannosaurus. Mobs in this list are immune to them. Apex predators are inherently immune so they aren't listed here");

        tyrannosaurusSpawnWeight = config.getInt("Tyrannosaurus Spawn Weight", "Tyrannosaurus", 6, 1, 100000000, "Spawn weight of Tyrannosaurus. Higher value = more common");
        tyrannosaurusSpawnBiomes = config.getStringList("Tyrannosaurus Spawn Biomes", "Tyrannosaurus", new String[]{"minecraft:plains", "minecraft:extreme_hills", "minecraft:smaller_extreme_hills"}, "List of biomes Tyrannosauruses will spawn in");
        tyrannosaurusFavoriteFood = config.getStringList("Tyrannosaurus Favorite Food", "Tyrannosaurus", new String[]{"minecraft:beef", "minecraft:cooked_beef", "minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:chicken", "minecraft:cooked_chicken", "minecraft:mutton", "minecraft:cooked_mutton", "minecraft:rabbit", "minecraft:cooked_rabbit", "minecraft:rotten_flesh", "rift:raw_exotic_meat", "rift:cooked_exotic_meat"}, "List of foods Tyrannosaurus will eat (when tamed) or pick up when on the ground");
        tyrannosaurusTargets = config.getStringList("Tyrannosaurus Targets", "Tyrannosaurus", new String[]{"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama"}, "Identifiers of mobs that the Tyrannosaurus will actively hunt");
        tyrannosaurusRoarTargetsWhitelist = config.getBoolean("Use Tyrannosaurus roar blacklist as a whitelist", "Tyrannosaurus", false, "Turn the blacklist of mobs affected by the Tyrannosaurus' roar into a whitelist of affected mobs");
        tyrannosaurusRoarTargetBlacklist = config.getStringList("Mobs unaffected by Tyrannosaurus roar", "Tyrannosaurus", new String[]{"rift:tyrannosaurus"}, "Mobs within this list are immune to this mobs knockback roar");
    }
}
