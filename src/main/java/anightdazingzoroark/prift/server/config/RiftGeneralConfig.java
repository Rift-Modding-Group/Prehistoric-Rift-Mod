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
    }

    public static class WorldGen {
        @Config.Name("Smokenut Bush Generation Weight")
        @Config.Comment({
                "Controls how often a smokenut bush patch generates.",
                "A value of 0 disables generation, 1 generates in one out of every eight chunks,",
                "and 8 generates in every chunk. Higher values make patches more likely."
        })
        @Config.RangeInt(min = 0, max = 8)
        public int smokenutBushWeight = 1;

        @Config.Name("Smokenut Bush Biomes")
        @Config.Comment({
                "Biomes where smokenut bushes can generate.",
                "Entries containing a colon are biome identifiers, such as minecraft:forest.",
                "Other entries are biome dictionary tags, such as FOREST."
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
