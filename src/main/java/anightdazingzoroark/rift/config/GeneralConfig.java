package anightdazingzoroark.rift.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfig  extends RiftConfig {
    public static String[] weakerThanWood = {"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"};
    public static boolean apexAffectedWhitelist = false;
    public static String[] apexAffectedBlacklist = {};
    public static String[] mountOverrideWhitelistItems = {"item:minecraft:bow:32767", "item:minecraft:shield:32767"};
    public static String[] herbivoreRegenEnergyFoods = {"rift:fiber_bar:0:4"};
    public static String[] carnivoreRegenEnergyFoods = {"rift:raw_fibrous_meat:0:2", "rift:cooked_fibrous_meat:0:4"};

    public GeneralConfig(Configuration config) {
        super(config, null, 1, 0);
    }

    @Override
    public void init() {
        weakerThanWood = config.getStringList("List of Ore Dictionary tags or Blocks weaker than wood", "General", new String[]{"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:workbench", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:chestWood"}, "Blocks and Ore Dictionary tags here will be considered weaker than wood, making them breakable by the Tyrannosaurus' roar and the like. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        apexAffectedWhitelist = config.getBoolean("Use Apex effects blacklist as a whitelist", "General", false, "Turn the blacklist of mobs affected by an apex predator's special effects into a whitelist of affected mobs");
        apexAffectedBlacklist = config.getStringList("Mobs unaffected by apex predator's effects", "General", new String[]{}, "Apex predators can apply some nasty debuffs to nearby mobs, like weakness for the Tyrannosaurus. Mobs in this list are immune to them. Apex predators are inherently immune so they aren't listed here");
        mountOverrideWhitelistItems = config.getStringList("List of items that can still be used while riding", "General", new String[]{"item:minecraft:bow:32767", "item:minecraft:shield:32767"}, "When riding a creature you can't use almost anything to make room for the controls of the creature being ridden. Items or Ore Dictionary tags in this list can still be used however. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add items add \"item:<insert item's identifier here>:<insert data id here>\". Food items can still be eaten while riding hence their exclusion");
        herbivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of herbivores", "General", new String[]{"rift:fiber_bar:0:4"}, "List of food items that can regenerate the energy of a herbivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");
        carnivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of carnivores", "General", new String[]{"rift:raw_fibrous_meat:0:2", "rift:cooked_fibrous_meat:0:4"}, "List of food items that can regenerate the energy of a carnivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");
    }
}