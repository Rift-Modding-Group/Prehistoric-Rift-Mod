package anightdazingzoroark.prift.server.config;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("WeakerAccess")
@Config(modid = RiftInitialize.MODID, name = RiftInitialize.MODID+"/general")
public class RiftGeneralConfig {
    @Config.Name("Creature Settings")
    @Config.Comment("Settings specific to creatures from the mod")
    public static final Creatures creatures = new Creatures();

    @Config.Name("World Generation Settings")
    @Config.Comment("Settings specific to world generation in the mod")
    public static final WorldGen worldGen = new WorldGen();

    @Config.Name("Other Settings")
    @Config.Comment("Miscellaneous settings")
    public static final Other other = new Other();

    public static class Creatures {
        @Config.Name("Mobs killed by wild creatures don't drop loot")
        @Config.Comment({
                "This is here to ensure that lag from frequent item",
                "drops from mobs killed by wild creatures won't occur.",
                "Disable at your own risk."
        })
        public boolean creatureKillNoLoot = true;

        @Config.Name("Maximum Creature Level")
        @Config.Comment({
                "Maximum level that a creature can reach",
                "Note that the mod has been scaled for a maximum level of 10",
                "Anything higher can cause some... issues..."
        })
        @Config.RangeInt(min = 1)
        public int maxLevel = 10;

        @Config.Name("Creature Level-Up Factor")
        @Config.Comment({
                "Level-Up factor that affects how much xp is required for all creatures",
                "to level up. Higher values means more XP required"
        })
        @Config.RangeInt(min = 1)
        public int levelUpFactor = 100;

        @Config.Name("Creature Box Revival Time")
        @Config.Comment("World-time seconds required to revive a boxed creature. 0 or negative revives immediately.")
        @Config.RangeInt(min = 0)
        public int boxRevivalTimeSeconds = 300;
    }

    public static class WorldGen {
        @Config.Name("Rifted Stone Mound Spawn Weight")
        @Config.Comment({
                "Percentage chance per Overworld chunk to attempt a Rifted Stone mound.",
                "Higher values make mounds more likely. 0 disables generation; 100 attempts one in every chunk.",
                "The default weight of 10 gives a 10% chance per chunk.",
                "Mounds only generate on suitable dry terrain with room for the entire mound inside the chunk."
        })
        @Config.RangeInt(min = 0, max = 100)
        public int riftedMoundWeight = 10;

        @Config.Name("Rifted Stone Mound Biomes")
        @Config.Comment({
                "Biomes where Rifted Stone mounds can generate.",
                "Entries containing a colon are biome identifiers, such as minecraft:forest.",
                "Other entries are biome dictionary tags, such as FOREST.",
                "The exact entry \"all\" allows every biome."
        })
        public String[] riftedMoundBiomes = {"all"};

        @Config.Name("Smokenut Bush Generation Weight")
        @Config.Comment({
                "Percentage chance per Overworld chunk to attempt a smokenut bush patch.",
                "Higher values make patches more likely. 0 disables generation; 100 attempts one in every chunk.",
                "The default weight of 13 gives a 13% chance per chunk.",
                "Patches only generate in configured biomes on suitable ground."
        })
        @Config.RangeInt(min = 0, max = 100)
        public int smokenutBushWeight = 13;

        @Config.Name("Smokenut Bush Biomes")
        @Config.Comment({
                "Biomes where smokenut bushes can generate.",
                "Entries containing a colon are biome identifiers, such as minecraft:forest.",
                "Other entries are biome dictionary tags, such as FOREST.",
                "The exact entry \"all\" allows every biome."
        })
        public String[] smokenutBushBiomes = {"FOREST"};

        @Config.Name("Smokenut Bush Concentration")
        @Config.Comment("The number of nearby placement attempts made for each smokenut bush patch.")
        @Config.RangeInt(min = 0, max = 64)
        public int smokenutBushConcentration = 8;
    }

    public static class Other {
        @Config.Name("Show Discord join message when opening a world")
        @Config.Comment({
                "Send to anyone who joins the world in chat a message",
                "linking to the official Rift Modding Discord server.",
                "Setting this to false will make me sad :("
        })
        public boolean showJoinDiscordMessage = true;
    }
}
