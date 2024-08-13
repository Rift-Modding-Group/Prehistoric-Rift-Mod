package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;

public class GeneralConfig {
    //general
    public static boolean showDiscordMessage = true;
    public static String[] weakerThanDirt = {"oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool", "oreDic:cropWheat", "oreDic:cropPotato", "oreDic:cropCarrot", "oreDic:cropNetherWart", "oreDic:sugarcane", "oreDic:blockCactus"};
    public static String[] weakerThanWood = {"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "block:minecraft:double_wooden_slab:-1", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:workbench", "oreDic:chestWood"};
    public static String[] weakerThanStone = {"oreDic:oreGold", "oreDic:oreIron", "oreDic:oreLapis", "oreDic:oreDiamond", "oreDic:oreRedstone", "oreDic:oreEmerald", "oreDic:oreQuartz", "oreDic:oreCoal", "oreDic:stone", "oreDic:cobblestone", "oreDic:sandstone", "oreDic:netherrack", "oreDic:glowstone", "oreDic:endstone", "oreDic:blockPrismarineBrick", "block:minecraft:stone_slab:-1", "block:minecraft:stone_slab2:-1", "block:minecraft:double_stone_slab:-1", "block:minecraft:double_stone_slab2:-1"};
    public static boolean apexAffectedWhitelist = false;
    public static String[] apexAffectedBlacklist = {};
    public static String[] mountOverrideWhitelistItems = {"item:minecraft:bow:-1", "item:minecraft:shield:-1"};
    public static String[] herbivoreRegenEnergyFoods = {"prift:fiber_bar:0:4"};
    public static String[] carnivoreRegenEnergyFoods = {"prift:raw_fibrous_meat:0:2", "prift:cooked_fibrous_meat:0:4"};
    public static boolean canDropFromCreatureKill = false;
    public static String[] universalCarnivoreTargets = {"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama", "minecraft:villager", "prift:stegosaurus", "prift:dodo", "prift:triceratops", "prift:parasaurolophus", "prift:megaloceros"};
    public static String[] turretModeHostileTargets = {"minecraft:cave_spider", "minecraft:enderman", "minecraft:spider", "minecraft:zombie_pigman", "minecraft:blaze", "minecraft:creeper", "minecraft:elder_guardian", "minecraft:endermite", "minecraft:evoker", "minecraft:ghast", "minecraft:guardian", "minecraft:husk", "minecraft:magma_cube", "minecraft:shulker", "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime", "minecraft:stray", "minecraft:vex", "minecraft:vindicator", "minecraft:witch", "minecraft:wither_skeleton", "minecraft:zombie", "minecraft:zombie_villager", "prift:tyrannosaurus", "prift:utahraptor", "prift:direwolf"};
    public static boolean dropHemolymph = true;
    public static boolean putDropsInCreatureInv = true;
    public static String minRevivalDiff = "HARD";

    //creature revival
    public static String[] reviveItems = {};

    //truffle stuff
    public static boolean truffleSpawning = true;
    public static String[] truffleBlocks = {"minecraft:grass:0"};
    public static double truffleChance = 0.5;
    public static String[] truffleBiomes = {"tag:forest"};
    public static String[] truffleAmntRange = {"1", "3"};

    //plant stuff
    public static String[] pyroberryBiomes = {"tag:plains"};
    public static int pyroberryWeight = 100;
    public static String[] cryoberryBiomes = {"tag:snowy"};
    public static int cryoberryWeight = 100;

    //creature leveling
    public static int levelingRadius = 800;
    public static int levelingRadisIncrement = 10;
    public static String[] difficultyIncrement = {"EASY:0", "NORMAL:5", "HARD:10"};

    //spawning
    public static String[] universalSpawnBlocks = {"minecraft:grass:0", "minecraft:dirt:-1", "minecraft:gravel:0", "minecraft:sand:-1", "minecraft:stone:-1", "minecraft:stained_hardened_clay:-1"};
    public static int dangerSpawnPreventRadius = 64;
    public static String[] dangerousMobs = {
            "prift:tyrannosaurus",
            "prift:utahraptor",
            "prift:megapiranha",
            "prift:sarcosuchus",
            "prift:direwolf",
            "prift:baryonyx"
    };
    public static int daysUntilDangerSpawnNearWSpawn = 3;
    public static int spawnAroundPlayerRad = 16;
    public static int spawnInterval = 400;

    //mod integration
    public static boolean mmIntegration = true;
    public static boolean pyrotechIntegration = true;
    public static boolean simpleDiffIntegration = true;

    //creature size
    public static String[] verySmallMobs = {"minecraft:bat", "minecraft:rabbit", "minecraft:silverfish", "minecraft:endermite", "minecraft:vex"};
    public static String[] smallMobs = {"prift:dodo", "prift:coelacanth", "prift:megapiranha", "prift:palaeocastor", "minecraft:pig", "minecraft:sheep", "minecraft:chicken", "minecraft:spider", "minecraft:cave_spider", "minecraft:squid", "minecraft:ocelot", "minecraft:parrot", "minecraft:shulker", "minecraft:wolf"};
    public static String[] mediumMobs = {"prift:utahraptor", "prift:parasaurolophus", "prift:dimetrodon", "prift:sarcosuchus", "prift:anomalocaris", "prift:direwolf", "prift:megaloceros", "minecraft:player", "minecraft:blaze", "minecraft:cow", "minecraft:creeper", "minecraft:donkey", "minecraft:enderman", "minecraft:evocation_illager", "minecraft:guardian", "minecraft:horse", "minecraft:husk", "minecraft:llama", "minecraft:mooshroom", "minecraft:mule", "minecraft:polar_bear", "minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:stray", "minecraft:villager", "minecraft:vindication_illager", "minecraft:witch", "minecraft:zombie", "minecraft:zombie_horse", "minecraft:zombie_pigman", "minecraft:zombie_villager"};
    public static String[] largeMobs = {"prift:stegosaurus", "prift:triceratops", "prift:saurophaganax", "prift:baryonyx", "minecraft:elder_guardian", "minecraft:wither_skeleton", "minecraft:wither"};
    public static String[] veryLargeMobs = {"prift:tyrannosaurus", "prift:apatosaurus", "minecraft:ghast", "minecraft:ender_dragon"};

    //debug
    public static boolean quickEggHatch = false;

    public static void readConfig() {
        Configuration config = RiftInitialize.configMain;
        try {
            config.load();
            init(config);
        }
        catch (Exception e1) {
            RiftInitialize.logger.log(Level.ERROR, "Problem loading config file!", e1);
        }
        finally {
            if (config.hasChanged()) config.save();
        }
    }

    public static void init(Configuration config) {
        //general
        showDiscordMessage = config.getBoolean("Show a message to join the Discord upon joining", "General", true, "JOIN THE DISCORD JOIN THE DISCORD JOIN THE DISCORD JOIN THE DISCORD JOIN THE DISCORD JOIN THE DISCORD JOIN THE DISCORD. pls don't set to false or i will be sad :(");

        //block breaking
        weakerThanDirt = config.getStringList("List of Ore Dictionary tags or Blocks weaker than dirt", "Block Breaking", new String[]{"oreDic:treeSapling", "oreDic:treeLeaves", "oreDic:vine", "oreDic:dirt", "oreDic:grass", "oreDic:gravel", "oreDic:sand", "oreDic:torch", "oreDic:blockSlime", "oreDic:blockGlassColorless", "oreDic:blockGlass", "oreDic:paneGlassColorless", "oreDic:paneGlass", "oreDic:wool"}, "Blocks and Ore Dictionary tags here will be considered weaker than dirt, making them breakable by certain weaker creatures. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        weakerThanWood = config.getStringList("List of Ore Dictionary tags or Blocks weaker than wood", "Block Breaking", new String[]{"oreDic:logWood", "oreDic:plankWood", "oreDic:slabWood", "block:minecraft:double_wooden_slab:-1", "oreDic:stairWood", "oreDic:fenceWood", "oreDic:fenceGateWood", "oreDic:doorWood", "oreDic:workbench", "oreDic:chestWood"}, "Blocks and Ore Dictionary tags here, alongside the ones specified in the weaker than dirt list, will be considered weaker than wood, making them breakable by the Tyrannosaurus' roar and the like. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        weakerThanStone = config.getStringList("List of Ore Dictionary tags or Blocks weaker than stone", "Block Breaking", new String[]{"oreDic:oreGold", "oreDic:oreIron", "oreDic:oreLapis", "oreDic:oreDiamond", "oreDic:oreRedstone", "oreDic:oreEmerald", "oreDic:oreQuartz", "oreDic:oreCoal", "oreDic:stone", "oreDic:cobblestone", "oreDic:sandstone", "oreDic:netherrack", "oreDic:glowstone", "oreDic:endstone", "oreDic:blockPrismarineBrick", "block:minecraft:stone_slab:-1", "block:minecraft:stone_slab2:-1", "block:minecraft:double_stone_slab:-1", "block:minecraft:double_stone_slab2:-1"}, "Blocks and Ore Dictionary tags here, alongside the ones specified in the weaker than dirt and wood lists, will be considered weaker than wood, making them breakable by some stronger creatures. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");

        //general (again idfk why)
        apexAffectedWhitelist = config.getBoolean("Use Apex effects blacklist as a whitelist", "General", false, "Turn the blacklist of mobs affected by an apex predator's special effects into a whitelist of affected mobs");
        apexAffectedBlacklist = config.getStringList("Mobs unaffected by apex predator's effects", "General", new String[]{}, "Apex predators can apply some nasty debuffs to nearby mobs, like weakness for the Tyrannosaurus. Mobs in this list are immune to them. Apex predators are inherently immune so they aren't listed here");
        mountOverrideWhitelistItems = config.getStringList("List of items that can still be used while riding", "General", new String[]{"item:minecraft:bow:-1", "item:minecraft:shield:-1"}, "When riding a creature you can't use almost anything to make room for the controls of the creature being ridden. Items or Ore Dictionary tags in this list can still be used however. To add Ore Dictionary tags add \"oreDic:<insert tag name here>\", while to add items add \"item:<insert item's identifier here>:<insert data id here>\". Food items can still be eaten while riding hence their exclusion");
        herbivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of herbivores", "General", new String[]{"prift:fiber_bar:0:4"}, "List of food items that can regenerate the energy of a herbivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");
        carnivoreRegenEnergyFoods = config.getStringList("List of food items that can regenerate energy of carnivores", "General", new String[]{"prift:raw_fibrous_meat:0:2", "prift:cooked_fibrous_meat:0:4"}, "List of food items that can regenerate the energy of a carnivore. To add items add \"<insert item's identifier here>:<insert data id here>:<insert amount of energy you want the item to regenerate>\"");
        canDropFromCreatureKill = config.getBoolean("Mobs killed by wild creatures drop loot", "General", false, "Mostly to try manage lag. This manages if mobs killed by wild creatures from this mod will drop their loot.");
        universalCarnivoreTargets = config.getStringList("List of mobs carnivores will attack", "General", new String[]{"minecraft:player", "minecraft:pig", "minecraft:chicken", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot", "minecraft:wolf", "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama", "minecraft:villager", "prift:stegosaurus", "prift:dodo", "prift:triceratops", "prift:parasaurolophus", "prift:megaloceros"}, "Identifiers of mobs that all carnivores will actively hunt");
        turretModeHostileTargets = config.getStringList("Hostile mobs targeted by mobs in turret mode", "General", new String[]{"minecraft:cave_spider", "minecraft:enderman", "minecraft:spider", "minecraft:zombie_pigman", "minecraft:blaze", "minecraft:creeper", "minecraft:elder_guardian", "minecraft:endermite", "minecraft:evoker", "minecraft:ghast", "minecraft:guardian", "minecraft:husk", "minecraft:magma_cube", "minecraft:shulker", "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime", "minecraft:stray", "minecraft:vex", "minecraft:vindicator", "minecraft:witch", "minecraft:wither_skeleton", "minecraft:zombie", "minecraft:zombie_villager", "prift:tyrannosaurus", "prift:utahraptor", "prift:direwolf"}, "Identifiers of mobs that creatures in turret mode will attack, if their targeting is set to \"Attack Hostiles\"");
        dropHemolymph = config.getBoolean("Let Arthropods drop Hemolymph and Chitin", "General", true, "Whether or not arthropods that are not added by Prehistoric Rift (basically anything Bane of Arthropods works on) will drop some Hemolymph and Chitin");
        putDropsInCreatureInv = config.getBoolean("Put drops of mobs killed by tamed creatures in their inventories", "General", true, "Whether or not items dropped by mobs killed by tamed creatures will automatically go to the inventory of whatever killed it");
        minRevivalDiff = config.getString("Minimum Difficulty for Creature Revival", "General", "HARD", "Minimum difficulty in which creatures can be incapacitated. If the difficulty is set to this or above, killed mobs just get killed. Usable values: PEACEFUL, EASY, NORMAL, HARD, NONE (tamed creatures can be revived regardless of difficulty).");

        //spawning
        universalSpawnBlocks = config.getStringList("List of blocks creatures can spawn on", "Spawning", new String[]{"minecraft:grass:0", "minecraft:dirt:-1", "minecraft:gravel:0", "minecraft:sand:-1", "minecraft:stone:-1", "minecraft:stained_hardened_clay:-1"}, "Identifiers of blocks that creatures from the mod can spawn on (water creatures don't count). To add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        dangerSpawnPreventRadius = config.getInt("Minimum radius from world spawn where dangerous creatures will not spawn in", "Spawning", 64, 1, 69420666, "To prevent new players from constant spawnkills on their first day, dangerous carnivores will not spawn in a certain radius (as defined here) surrounding spawn as defined here for some time");
        dangerousMobs = config.getStringList("Dangerous mobs to not spawn near world spawn", "Spawning", new String[]{
                "prift:tyrannosaurus",
                "prift:utahraptor",
                "prift:megapiranha",
                "prift:sarcosuchus",
                "prift:direwolf",
                "prift:baryonyx"
        }, "Mobs in this list will not spawn in a radius around world spawn for some time");
        daysUntilDangerSpawnNearWSpawn = config.getInt("Days until dangerous creatures can spawn near world spawn", "Spawning", 3, 1, 69420666, "Creatures will not spawn near world spawn for the following value in days");
        spawnAroundPlayerRad = config.getInt("Radius from players that new creatures spawn in", "Spawning", 16, 1, 69420666, "Creatures will not spawn in a radius of this value around the player");
        spawnInterval = config.getInt("Spawning Interval", "Spawning", 400, 1, 69420666, "Interval in ticks new creatures can spawn around players");

        //truffle stuff
        truffleSpawning = config.getBoolean("Truffles can spawn", "Truffle Spawning Stuff", true, "Whether or not truffles can drop by breaking blocks in certain biomes");
        truffleBlocks = config.getStringList("Blocks truffles can spawn from", "Truffle Spawning Stuff", new String[]{"minecraft:grass:0"}, "Blocks that, if broken when in certain biomes, will spawn truffles. To add blocks add \"block:<insert block's identifier here>:<insert data id here>\"");
        truffleChance = config.getFloat("Chance for truffle spawning", "Truffle Spawning Stuff", 0.5f, 0f, 1f, "Chance for truffles to spawn from breaking certain blocks");
        truffleBiomes = config.getStringList("Biomes where truffles can be found", "Truffle Spawning Stuff", new String[]{"tag:forest"}, "Biomes that truffles will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
        truffleAmntRange = config.getStringList("Range for amount of truffles dropped", "Truffle Spawning Stuff", new String[]{"1", "3"}, "The range for the amount of truffles that can spawn from breaking blocks.");

        //plant stuff
        pyroberryBiomes = config.getStringList("Pyroberry Biomes", "Plant Stuff", new String[]{"tag:plains"}, "The biomes that Pyroberries will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
        pyroberryWeight = config.getInt("Pyroberry Spawn Weight", "Plant Stuff", 100, 1, 69420666, "The weight representing how likely Pyroberries spawn. Higher values mean higher amounts of Pyroberries");
        cryoberryBiomes = config.getStringList("Cryoberry Biomes", "Plant Stuff", new String[]{"tag:snowy"}, "The biomes that Pyroberries will spawn in. To add an entry add \"<biome/tag>:<insert identifier of biome or name of biome tag here>\". To blacklist an entry (make it so it will never spawn there) add \"-<biome/tag>:<insert identifier of biome or name of biome tag here>\"");
        cryoberryWeight = config.getInt("Cryoberry Spawn Weight", "Plant Stuff", 100, 1, 69420666, "The weight representing how likely Cryoberries spawn. Higher values mean higher amounts of Pyroberries");

        //creature leveling
        levelingRadius = config.getInt("Level based on distance from (0, 0)", "Creature Leveling", 1600, 0, 69420666, "Distance from (0, 0) from which creature levels get increased. This makes it so that the farther you are from the center, the higher the levels of wild creatures are.");
        levelingRadisIncrement = config.getInt("Level increment based on distance from (0, 0)", "Creature Leveling", 10, 0, 69420666, "Level increment based on distance from (0, 0). This makes it so that the farther you are from the center, the higher the levels of wild creatures are.");
        difficultyIncrement = config.getStringList("Level increment based on difficulty", "Creature Leveling", new String[]{"EASY:0", "NORMAL:5", "HARD:5"}, "Level increment based on the difficulty setting of the world. Only the number may be edited, changing the names can cause crashes. Note that the numbers are additive, so if Easy, Normal, and Hard are set to 5, the increment will be 15. Changing the order of the strings may also cause a crash.");

        //mod integration
        mmIntegration = config.getBoolean("Activate Mystical Mechanics Integration", "Mod Integration", true, "Whether or not additional features will be turned on if Mystical Mechanics is detected in the mods folder");
        pyrotechIntegration = config.getBoolean("Activate Pyrotech Integration", "Mod Integration", true, "Whether or not additional features will be turned on if Pyrotech is detected in the mods folder");
        simpleDiffIntegration = config.getBoolean("Activate Simple Difficulty Integration", "Mod Integration", true, "Whether or not additional features will be turned on if Simple Difficulty is detected in the mods folder");

        //mob size
        config.addCustomCategoryComment("Mob Size", "Order of sizes: very small -> small -> medium -> large -> very large. Mobs that are not listed amongst any of the sizes are automatically considered medium size.");
        verySmallMobs = config.getStringList("Very Small Mobs", "Mob Size", new String[]{"minecraft:bat", "minecraft:rabbit", "minecraft:silverfish", "minecraft:endermite", "minecraft:vex"}, "Mobs that are to be of very small size");
        smallMobs = config.getStringList("Small Mobs", "Mob Size", new String[]{"prift:dodo", "prift:coelacanth", "prift:megapiranha", "prift:palaeocastor", "minecraft:pig", "minecraft:sheep", "minecraft:chicken", "minecraft:spider", "minecraft:cave_spider", "minecraft:squid", "minecraft:ocelot", "minecraft:parrot", "minecraft:shulker", "minecraft:wolf"}, "Mobs that are to be of small size");
        mediumMobs = config.getStringList("Medium Mobs", "Mob Size", new String[]{"prift:utahraptor", "prift:parasaurolophus", "prift:dimetrodon", "prift:sarcosuchus", "prift:anomalocaris", "prift:direwolf", "prift:megaloceros", "minecraft:player", "minecraft:blaze", "minecraft:cow", "minecraft:creeper", "minecraft:donkey", "minecraft:enderman", "minecraft:evocation_illager", "minecraft:guardian", "minecraft:horse", "minecraft:husk", "minecraft:llama", "minecraft:mooshroom", "minecraft:mule", "minecraft:polar_bear", "minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:stray", "minecraft:villager", "minecraft:vindication_illager", "minecraft:witch", "minecraft:zombie", "minecraft:zombie_horse", "minecraft:zombie_pigman", "minecraft:zombie_villager"}, "Mobs that are to be of medium size");
        largeMobs = config.getStringList("Large Mobs", "Mob Size", new String[]{"prift:stegosaurus", "prift:triceratops", "prift:saurophaganax", "prift:baryonyx", "minecraft:elder_guardian", "minecraft:wither_skeleton", "minecraft:wither"}, "Mobs that are to be of large size");
        veryLargeMobs = config.getStringList("Very Large Mobs", "Mob Size", new String[]{"prift:tyrannosaurus", "prift:apatosaurus", "minecraft:ghast", "minecraft:ender_dragon"}, "Mobs that are to be of very large size");

        //debug
        quickEggHatch = config.getBoolean("All eggs hatch quickly", "Debug", false, "Turning this on makes all eggs hatch within 5 seconds. Mainly here for testing purposes, idk i could have made this a gamerule or smth");
    }

    public static boolean canUseMM() {
        return mmIntegration && Loader.isModLoaded(RiftInitialize.MYSTICAL_MECHANICS_MOD_ID);
    }

    public static boolean canUsePyrotech() {
        return pyrotechIntegration && Loader.isModLoaded(RiftInitialize.PYROTECH_MOD_ID);
    }

    public static boolean canUseSimpleDiff() {
        return simpleDiffIntegration && Loader.isModLoaded(RiftInitialize.SIMPLE_DIFFICULTY_MOD_ID);
    }
}