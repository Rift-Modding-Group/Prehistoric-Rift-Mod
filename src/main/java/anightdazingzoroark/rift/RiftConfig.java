package anightdazingzoroark.rift;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class RiftConfig {
    public static String[] weakerThanWood = {"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"};
    public static boolean apexAffectedWhitelist = false;
    public static String[] apexAffectedBlacklist = {};
    public static String[] mountOverrideWhitelistItems = {"item:minecraft:bow:32767", "item:minecraft:shield:32767"};
    public static String[] herbivoreRegenEnergyFoods = {"rift:fiber_bar:0:4"};
    public static String[] carnivoreRegenEnergyFoods = {"rift:raw_fibrous_meat:0:2", "rift:cooked_fibrous_meat:0:4"};

    public static String[] tyrannosaurusFavoriteFood = {"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "rift:raw_exotic_meat:0:0.05", "rift:cooked_exotic_meat:0:0.075", "rift:raw_fibrous_meat:0:0", "rift:cooked_fibrous_meat:0:0"};
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
        weakerThanWood = config.getStringList("List of Ore Dictionary tags or Blocks weaker than wood", "Misc", new String[]{"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"}, "Blocks and Ore Dictionary tags here will be considered weaker than wood, making them breakable by the Tyrannosaurus' roar and the like. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        apexAffectedWhitelist = config.getBoolean("Use Apex effects blacklist as a whitelist", "Misc", false, "Turn the blacklist of mobs affected by an apex predator's special effects into a whitelist of affected mobs");
        apexAffectedBlacklist = config.getStringList("Mobs unaffected by apex predator's effects", "Misc", new String[]{}, "Apex predators can apply some nasty debuffs to nearby mobs, like weakness for the Tyrannosaurus. Mobs in this list are immune to them. Apex predators are inherently immune so they aren't listed here");
        mountOverrideWhitelistItems = config.getStringList("List of items that can still be used while riding", "Misc", new String[]{"item:minecraft:bow:32767", "item:minecraft:shield:32767"}, "When riding a creature you can't use almost anything to make room for the controls of the creature being ridden. Items or Ore Dictionary tags in this list can still be used however. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add items add \"item:<insert item's identifier here>:<insert data id here>\". Food items can still be eaten while riding hence their exclusion");
        herbivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of herbivores", "Misc", new String[]{"rift:fiber_bar:0:4"}, "List of food items that can regenerate the energy of a herbivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");
        carnivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of carnivores", "Misc", new String[]{"rift:raw_fibrous_meat:0:2", "rift:cooked_fibrous_meat:0:4"}, "List of food items that can regenerate the energy of a carnivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");

        tyrannosaurusFavoriteFood = config.getStringList("Tyrannosaurus Favorite Food", "Tyrannosaurus", new String[]{"minecraft:beef:0:0.05", "minecraft:cooked_beef:0:0.075", "minecraft:porkchop:0:0.05", "minecraft:cooked_porkchop:0:0.075", "minecraft:chicken:0:0.05", "minecraft:cooked_chicken:0:0.075", "minecraft:mutton:0:0.05", "minecraft:cooked_mutton:0:0.075", "minecraft:rabbit:0:0.05", "minecraft:cooked_rabbit:0:0.075", "minecraft:rotten_flesh:0:0.075", "rift:raw_exotic_meat:0:0.05", "rift:cooked_exotic_meat:0:0.075", "rift:raw_fibrous_meat:0:0", "rift:cooked_fibrous_meat:0:0"}, "List of foods Tyrannosaurus will eat (when tamed) or pick up when on the ground. To add items add \"<insert item's identifier here>:<insert data id here>:<insert percentage of health that will be healed upon consumption here>\"");
        tyrannosaurusTargets = config.getStringList("Tyrannosaurus Targets", "Tyrannosaurus", new String[]{"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama"}, "Identifiers of mobs that the Tyrannosaurus will actively hunt");
        tyrannosaurusRoarTargetsWhitelist = config.getBoolean("Use Tyrannosaurus roar blacklist as a whitelist", "Tyrannosaurus", false, "Turn the blacklist of mobs affected by the Tyrannosaurus' roar into a whitelist of affected mobs");
        tyrannosaurusRoarTargetBlacklist = config.getStringList("Mobs unaffected by Tyrannosaurus roar", "Tyrannosaurus", new String[]{"rift:tyrannosaurus"}, "Mobs within this list are immune to this mobs knockback roar");
    }
}
